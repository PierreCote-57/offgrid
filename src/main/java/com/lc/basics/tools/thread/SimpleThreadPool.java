/*
 * Copyright (c) 2017 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */
package com.lc.basics.tools.thread;

import com.lc.basics.monitoring.counter.OperationContext;
import com.lc.basics.monitoring.counter.OperationCounter;
import com.lc.basics.tools.file.BaseFileHandler;
import com.lc.basics.tools.misc.BasicRuntimeException;
import com.lc.basics.tools.misc.BasicTools;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleThreadPool extends ThreadPoolExecutor
{
	protected static final QueueFactory<Runnable>	QUEUE_FACTORY			= new QueueFactory<>();
	private static final SimpleThreadPool COMMON_POOL = createThreadPool(
			"Common", BasicTools.getNumberOfProcessors(), 0);

	private final String						m_poolName;
	private final int							m_poolSizeMax;
	private final int							m_queueSizeMax;
	private final BlockingQueue<Runnable>		m_queue;
	private final AtomicInteger					m_countExecuting		= new AtomicInteger(0);

	private OperationCounter					m_queueCounter;
	private OperationCounter					m_runningCounter;
	private MySemaphore							m_semaphore;

	protected SimpleThreadPool(String poolName, int corePoolSize, int queueSizeMax,
							 BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory)
	{
		super(corePoolSize, corePoolSize, 0, TimeUnit.SECONDS, workQueue,
			threadFactory);

		m_poolName = poolName;
		m_poolSizeMax = corePoolSize;
		m_queueSizeMax = queueSizeMax;
		m_queue = workQueue;
		prestartAllCoreThreads();

		m_semaphore = new MySemaphore(poolName, corePoolSize);
		createCounters();
	}

	// Delayed counter creation to enable startup sequence which would otherwise be circular.
	private void createCounters()
	{
		synchronized (this)		// Create the counters only once.
		{
			if (null == m_runningCounter)
			{
				String[]				counterNameList		= new String[3];
				counterNameList[0] = getClass().getSimpleName();
				counterNameList[1] = m_poolName;

				counterNameList[2] = "InQueue";
				m_queueCounter = new OperationCounter(counterNameList);

				counterNameList[2] = "Running";
				m_runningCounter = new OperationCounter(counterNameList);
			}
		}
	}

	public static SimpleThreadPool createThreadPool(String poolName, int poolSize, int queueSize)
	{
		BlockingQueue<Runnable>		queue		= QUEUE_FACTORY.getBlockingQueue(queueSize);
		ThreadFactory				factory		= new SimpleThreadFactory(poolName, poolSize);
		SimpleThreadPool			pool		= new SimpleThreadPool(poolName, poolSize, queueSize, queue, factory);

		return pool;
	}
	public static SimpleThreadPool getCommonPool()
	{
		return COMMON_POOL;
	}

	public MySemaphore getSemaphore()
	{
		return m_semaphore;
	}
	public int getPoolSizeMax()
	{
		return m_poolSizeMax;
	}
	public int getQueueSizeMax()
	{
		return m_queueSizeMax;
	}
	public int getQueueSize()
	{
		return m_queue.size();
	}
	public int getCountExecuting()
	{
		return m_countExecuting.get();
	}
	public boolean isIdle()
	{
		return 0 == getQueueSize() && 0 == getCountExecuting();
	}
	public void waitForIdle()
	{
		while (!isIdle())
		{
			BasicTools.sleepMS(1_000);
		}
	}

	@Override
	public void execute(Runnable runnable)
	{
		// Delayed counter creation to enable startup sequence which would otherwise be circular.
		RunnableWrapper			wrapper			= new RunnableWrapper();
		wrapper.setRunnable(this, runnable);

		try
		{
			super.execute(wrapper);
		}
		catch (RuntimeException exception)
		{
			String		message		= String.format("Unable to post (execute) Runnable to thread pool %s.", m_poolName);
			throw new BasicRuntimeException(exception, message);
		}
	}

	@Override
	public Future<?> submit(Runnable runnable)
	{
		throw new UnsupportedOperationException("Use execute() instead of submit.");
	}

	@Override
	public void shutdown()
	{
		m_queueCounter		= BaseFileHandler.closeSafe(m_queueCounter);
		m_runningCounter	= BaseFileHandler.closeSafe(m_runningCounter);
		m_semaphore			= BaseFileHandler.closeSafe(m_semaphore);

		super.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow()
	{
		m_queueCounter		= BaseFileHandler.closeSafe(m_queueCounter);
		m_runningCounter	= BaseFileHandler.closeSafe(m_runningCounter);
		m_semaphore			= BaseFileHandler.closeSafe(m_semaphore);

		return super.shutdownNow();
	}

	/**
	 * The main purpose of this class is to name the threads appropriately.
	 *
	 * @author Pierre
	 *
	 */
	protected static class SimpleThreadFactory implements ThreadFactory
	{
		private final String		m_name;
		private int 				m_poolSize;
		private int					m_count = 0;

		public SimpleThreadFactory(String name, int poolSize)
		{
			m_name = name;
			m_poolSize = poolSize;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
		 */
		public Thread newThread(Runnable r)
		{
			Thread t = new Thread(r);

			t.setName(String.format("%s-%d/%d", m_name, m_count++, m_poolSize));
			t.setDaemon(true);

			return t;
		}
	}

	// Must be a static class to extend AbstractPooledObject.
	public static class RunnableWrapper implements Runnable
	{
		private SimpleThreadPool		m_threadPool			= null;
		private Runnable				m_wrappedRunnable		= null;

		private OperationContext m_inQueueContext		= null;

		public RunnableWrapper()
		{
		}

		public void setRunnable(SimpleThreadPool threadPool, Runnable wrappedRunnable)
		{
			m_threadPool = threadPool;
			m_wrappedRunnable = wrappedRunnable;

			m_inQueueContext = m_threadPool.m_queueCounter.begin();
		}

		@Override
		public void run()
		{
			assert null != m_threadPool;
			assert null != m_wrappedRunnable;
			assert null != m_inQueueContext;

			m_threadPool.getSemaphore().enter();
			try (OperationContext context = m_threadPool.m_runningCounter.begin())
			{
				m_inQueueContext.end(true);
				m_inQueueContext = null;		// ended.

				m_threadPool.m_countExecuting.incrementAndGet();

				m_wrappedRunnable.run();
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
			finally
			{
				m_threadPool.m_countExecuting.decrementAndGet();
				MySemaphore		semaphore		= m_threadPool.getSemaphore();
				if (null != semaphore)		// Can occur when closing ThreadPool
				{
					semaphore.leave();
				}
			}
		}
	}
}
