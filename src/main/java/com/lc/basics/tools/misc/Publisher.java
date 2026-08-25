/*
 * Copyright (c) 2014 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.misc;

import com.lc.basics.tools.logging.BasicLogger;
import com.lc.basics.tools.thread.SimpleThreadPool;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Publisher
{
	private static final BasicLogger LOGGER			= BasicLogger.getLogger(Publisher.class);
	private static final Map<String, Publisher>		PUBLISHER_MAP	= new HashMap<>();
	private static final SimpleThreadPool			THREAD_POOL;

	private final String m_name;
	private final List<EventListener> m_listenerList		= new LinkedList<>();

	static
	{
		THREAD_POOL = SimpleThreadPool.createThreadPool("Publisher", 2, 100);
	}

	private Publisher(String name)
	{
		m_name = name;
	}

	public static BasicLogger getLogger()
	{
		return LOGGER;
	}

	public static Publisher getPublisher(String name)
	{
		synchronized (PUBLISHER_MAP)
		{
			Publisher		publisher		= PUBLISHER_MAP.get(name);
			if (null == publisher)
			{
				publisher = new Publisher(name);
				PUBLISHER_MAP.put(name, publisher);
			}
			return publisher;
		}
	}

	public String getName()
	{
		return m_name;
	}

	public void register(EventListener listener)
	{
		getLogger().debug("register(%s, %s)", getName(), listener.toString());

		synchronized (m_listenerList)
		{
			m_listenerList.add(listener);
		}
	}
	public void deregister(EventListener listener)
	{
		getLogger().debug("deregister(%s, %s)", getName(), listener.toString());

		synchronized (m_listenerList)
		{
			m_listenerList.remove(listener);
		}
	}

	/**
	 * post places the notification in a queue that is processed by a thread pool.
	 * The processing of the notification by the subscribers is done in the thread of the thread pool.
	 * The post call returns immediately and is virtually free (execution time wise) to the publisher.
	 *
	 * @param timeMS	Time of the notification
	 * @param event		Event relevant to the publisher
	 */
	public void post(long timeMS, Object event)
	{
		if (0 == timeMS)
		{
			timeMS = System.currentTimeMillis();
		}

		getLogger().trace("%s publishing %s", m_name, event);

		THREAD_POOL.execute(new EventRunnable(this, timeMS, event));
	}

	public static void waitForPublishComplete()
	{
		while (0 != THREAD_POOL.getQueueSize() || 0 != THREAD_POOL.getCountExecuting())
		{
			BasicTools.sleepMS(1);
		}
	}

	/**
	 * Publishes the notification immediately, in the publisher's thread.
	 * Note that the publisher must wait until all subscribers have processed the notification
	 * before proceeding.
	 *
	 * @param timeMS	Time of the notification
	 * @param event		Event relevant to the publisher
	 */
	public void publish(long timeMS, Object event)
	{
		// Make a copy of the list inside the lock, then walk the copy outside the lock,
		// ... to minimize lock time AND to avoid deadlock with unspecified threads registering themselves
		List<EventListener> listenerList;

//		IMonitorContext			context			= MONITOR_FAMILY_PUBLISH.begin(m_name);
		boolean 				isSuccess		= true;
		synchronized (m_listenerList)
		{
			listenerList = new LinkedList<>(m_listenerList);
		}
		for (EventListener listener : listenerList)
		{
			try
			{
				listener.receiveEvent(m_name, timeMS, event);
			}
			catch (Throwable throwable)
			{
				getLogger().error(throwable, "Listener has thrown while receiving event %s", event);
				isSuccess = false;
			}
		}

//		MONITOR_FAMILY_PUBLISH.end(context, isSuccess, listenerList.size());
	}






	public interface EventListener
	{
		void receiveEvent(String publisherName, long timeMS, Object event);
	}





	public static class EventRunnable implements Runnable
	{
		private Publisher		m_publisher;
		private long			m_timeMS;
		private Object m_event;

		public EventRunnable(Publisher publisher, long timeMS, Object event)
		{
			m_publisher = publisher;
			m_timeMS = timeMS;
			m_event = event;
		}

		@Override
		public void run()
		{
			m_publisher.publish(m_timeMS, m_event);
		}
	}
}
