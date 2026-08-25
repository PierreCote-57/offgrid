/*
 * Copyright (c) 2014 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.thread;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class QueueFactory<T>
{
	public BlockingQueue<T> getBlockingQueue(int size)
	{
		if (0 == size)
		{
			return getUnboundQueue();
		}
		else
		{
			return getArrayBlockingQueue(size);
		}
	}

	public Queue<T> getQueue(int size)
	{
		if (0 == size)
		{
			return getUnboundQueue();
		}
		else
		{
			return getArrayBlockingQueue(size);
		}
	}

	public BlockingQueue<T> getUnboundQueue()
	{
		return new LinkedBlockingQueue<>();
	}

	public BlockingQueue<T> getArrayBlockingQueue(int size)
	{
		return new ArrayBlockingQueue<>(size, true);
	}
}
