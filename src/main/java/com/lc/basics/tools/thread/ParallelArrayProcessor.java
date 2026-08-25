/*
 * Copyright (c) 2020 LogicielCote.COM Systems All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.thread;

import com.lc.basics.tools.misc.BasicTools;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

public class ParallelArrayProcessor
{
	private static final int						ITEM_PER_RUNNABLE	= 10;
	private static final int						NUMBER_OF_CORES		= BasicTools.getNumberOfProcessors();
	private static final SimpleThreadPool			THREAD_POOL			=
			SimpleThreadPool.createThreadPool(ParallelArrayProcessor.class.getSimpleName(), NUMBER_OF_CORES, 0);
	static
	{
		THREAD_POOL.getSemaphore().setCountMax(NUMBER_OF_CORES);
	}

	public static <Q, A> A[] processArray(Q[] questionList, A[] answerList, Function<Q, A> function) throws InterruptedException
	{
		if (questionList.length > answerList.length)
		{
			answerList = Arrays.copyOf(answerList, questionList.length);
		}
		int				runnableCount		= countRunnable(questionList.length);
		int				keysPerRunnable		= 10;

		CountDownLatch	latch				= new CountDownLatch(runnableCount);
		for (int i = 0; i < runnableCount; i++)
		{
			int							iMin			= i * keysPerRunnable;
			int							iMax			= Math.min(iMin + keysPerRunnable, questionList.length);
			ParallelProcessor<Q, A>		runnable		= new ParallelProcessor<>(latch, function, questionList, answerList, iMin, iMax);
			if (iMin >= iMax)
			{
				latch.countDown();
			}
			else
			{
				THREAD_POOL.execute(runnable);
			}
		}
		latch.await();

		// must return answerList, because it may have changed
		return answerList;
	}

	public static int countRunnable(int size)
	{
		int		count		= size / ITEM_PER_RUNNABLE;
		while (count * ITEM_PER_RUNNABLE < size)
		{
			count++;
		}
		return count;
	}





	private static class ParallelProcessor<Q, A> implements Runnable
	{
		private final CountDownLatch		m_latch;
		private final Function<Q, A>		m_function;
		private final Q[]					m_questionList;
		private final A[]					m_answerList;
		private final int					m_iMin;
		private final int					m_iMax;

		public ParallelProcessor(CountDownLatch latch, Function<Q, A> function, Q[] questionList, A[] answerList, int iMin, int iMax)
		{
			m_latch = latch;
			m_function = function;
			m_questionList = questionList;
			m_answerList = answerList;
			m_iMin = iMin;
			m_iMax = iMax;
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
			for (int i = m_iMin; i < m_iMax; i++)
			{
				try
				{
					m_answerList[i] = m_function.apply(m_questionList[i]);
				}
				catch (Exception e)
				{
					m_answerList[i] = null;
				}
			}
			m_latch.countDown();
		}
	}
}
