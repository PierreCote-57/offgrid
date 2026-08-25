/*
 * Copyright (c) 2021 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.thread;

import com.lc.basics.tools.misc.BasicRuntimeException;
import com.lc.basics.tools.time.WallClock;
import com.lc.basics.tools.time.WallClock.FormatSize;
import com.lc.basics.tools.time.WallClock.FormatType;
import com.lc.basics.tools.units.TimeUnits;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class BackgroundThreadPool<T extends BackgroundThreadPool.BackgroundTask> extends SimpleThreadPool
{
	private static final AtomicInteger		LAST_TASK_COUNTER		= new AtomicInteger(0);
	private final Map<Integer, T>			m_taskMap				= new TreeMap<>();

	public static class BackgroundTask implements Runnable, Comparable<BackgroundTask>
	{
		private final int m_index = LAST_TASK_COUNTER.incrementAndGet();
		private final Runnable m_runnable;

		private String m_name;
		private String m_comment = "DefaultComment";
		private String m_commentTitle = "DefaultCommentTitle";
		private final long m_submitTimeMS;
		private long m_startTimeMS;
		private long m_stopTimeMS;
		private Exception m_exception;

		public BackgroundTask(String name, Runnable runnable)
		{
			m_name = name;
			m_runnable = runnable;

			m_submitTimeMS = System.currentTimeMillis();
		}

		public void setComment(String comment)
		{
			m_comment = comment;
		}

		public void setCommentTitle(String commentTitle)
		{
			m_commentTitle = commentTitle;
		}

		public int getIndex()
		{
			return m_index;
		}

		public void setName(String name)
		{
			m_name = name;
		}

		public String getName()
		{
			return m_name;
		}

		public String getComment()
		{
			return m_comment;
		}

		public String getCommentTitle()
		{
			return m_commentTitle;
		}

		public Runnable getRunnable()
		{
			return m_runnable;
		}

		public long getSubmitTimeMS()
		{
			return m_submitTimeMS;
		}

		public String getSubmitTimeText()
		{
			return WallClock.formatTime(FormatType.Time, FormatSize.Medium, getSubmitTimeMS());
		}

		public String getSubmitDateTimeText()
		{
			return WallClock.formatTime(FormatType.DateTime, FormatSize.Medium, getSubmitTimeMS());
		}

		public long getStartTimeMS()
		{
			return m_startTimeMS;
		}

		public String getStartTimeText()
		{
			return 0 == getStartTimeMS()
					? "N/A"
					: WallClock.formatTime(FormatType.Time, FormatSize.Medium, getStartTimeMS());
		}

		public String getStartDateTimeText()
		{
			return 0 == getStartTimeMS()
					? "N/A"
					: WallClock.formatTime(FormatType.DateTime, FormatSize.Medium, getStartTimeMS());
		}

		public long getStopTimeMS()
		{
			return m_stopTimeMS;
		}

		public String getStopTimeText()
		{
			return 0 == getStopTimeMS()
					? "N/A"
					: WallClock.formatTime(FormatType.Time, FormatSize.Medium, getStopTimeMS());
		}

		public String getStopDateTimeText()
		{
			return 0 == getStopTimeMS()
					? "N/A"
					: WallClock.formatTime(FormatType.DateTime, FormatSize.Medium, getStopTimeMS());
		}

		public long getDuration()
		{
			long startTime = getStartTimeMS();
			long stopTime = getStopTimeMS();

			long durationMS;
			if (0 == startTime)
			{
				durationMS = 0;
			} else if (0 == stopTime)
			{
				durationMS = System.currentTimeMillis() - startTime;
			} else
			{
				durationMS = stopTime - startTime;
			}
			return durationMS;
		}

		public String getDurationText()
		{
			return TimeUnits.MS.format(getDuration());
		}

		public Exception getException()
		{
			return m_exception;
		}

		/**
		 * When an object implementing interface <code>Runnable</code> is used
		 * to create a thread, starting the thread causes the object's
		 * <code>run</code> method to be called in that separately executing
		 * thread.
		 * <p>
		 * The general contract of the method <code>run</code> is that it may
		 * take any action whatsoever.
		 *
		 * @see Thread#run()
		 */
		@Override
		public void run()
		{
			m_startTimeMS = System.currentTimeMillis();
			try
			{
				getRunnable().run();
			}
			catch (Exception exception)
			{
				m_exception = exception;
				setComment("Exception: " + exception);
			}
			m_stopTimeMS = System.currentTimeMillis();
		}

		@Override
		public int compareTo(BackgroundTask that)
		{
			return Integer.compare(this.getIndex(), that.getIndex());
		}

		@Override
		public String toString()
		{
			if (0 == getStopTimeMS())
			{
				return String.format("%-70s is NOT complete", getName());
			}
			else
			{
				return String.format("%-70s complete completed in %s",
						getName(), getDurationText());
			}
		}
	}

	private BackgroundThreadPool(String poolName, int corePoolSize, int queueSizeMax,
							 BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory)
	{
		super(poolName, corePoolSize, queueSizeMax, workQueue, threadFactory);
	}

	public static <T extends BackgroundTask> BackgroundThreadPool<T> createBackgroundThreadPool(String poolName, int poolSize, int queueSize)
	{
		BlockingQueue<Runnable>		queue		= QUEUE_FACTORY.getBlockingQueue(queueSize);
		ThreadFactory				factory		= new SimpleThreadFactory(poolName, poolSize);
		BackgroundThreadPool<T>		pool		= new BackgroundThreadPool<>(poolName, poolSize, queueSize, queue, factory);

		return pool;
	}

	public Map<Integer, T> getTaskMap()
	{
		return m_taskMap;
	}

	@Override
	public void execute(Runnable runnable)
	{
		throw new BasicRuntimeException("Must give the runnable a name");
	}
	public void execute(T task)
	{
		synchronized (getTaskMap())
		{
			getTaskMap().put(task.getIndex(), task);
		}

		super.execute(task);
	}
	public T removeTask(int index)
	{
		synchronized (getTaskMap())
		{
			return getTaskMap().remove(index);
		}
	}

	@Override
	public String toString()
	{
		String fromSuper =  super.toString();

		return fromSuper;
	}
}
