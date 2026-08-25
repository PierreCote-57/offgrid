/*
 * Copyright (c) 2015 LogicielCote.COM Systems All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.time;

import com.lc.basics.tools.logging.BasicLogger;

public class TimeTraveler extends Thread
{
	private static final BasicLogger LOGGER					= BasicLogger.getLogger(TimeTraveler.class);
	private static final int 				DURATION_MS				= 5000;
	private static volatile Boolean			s_isTimeTraveler		= null;

	static
	{
		new TimeTraveler();
	}

	private TimeTraveler()
	{
		setDaemon(true);
		start();
	}

	public static BasicLogger getLogger()
	{
		return LOGGER;
	}

	/**
	 * Causes the class to be loaded and measurement to start without blocking the calling thread.
	 * The static block does the work.
	 */
	public static void measureTimeTravel()
	{
		// No work to do here.
	}

	/**
	 * If true, indicates the nano clock, System.nanoTime(), can move backwards.
	 * This occurs on some XP/AMD systems and can generally be fixed with a boot.ini fix.
	 * In any case, it is undesirable and causes problems to the counters code...
	 * It should be corrected.
	 *
	 * The value is only measured once, then it is cached. The cached value is measured.
	 * The fix requires a reboot.
	 *
	 * @return		True = bad: The clock can move backwards; False = good: The clock continuous moves forward.
	 */
	public static boolean isTimeTraveler()
	{
		// Wait until isTimeTraveler is determined
		while (null == s_isTimeTraveler)
		{
			try
			{
				Thread.sleep(10);
			}
			catch (InterruptedException e)
			{
				// Keep waiting
			}
		}

		return s_isTimeTraveler;
	}

	@Override
	public void run()
	{
		String threadName		= getClass().getSimpleName();
		setName(threadName);

		long		startTime		= System.currentTimeMillis();
		long		endTime			= startTime + DURATION_MS;
		long		timeNanoSav		= System.nanoTime();

		while (System.currentTimeMillis() < endTime)
		{
			long	timeNano		= System.nanoTime();
			if (timeNano < timeNanoSav)		// == allowed on REALLY fast systems :)
			// OR on systems who only report time to the us.
			{
				s_isTimeTraveler = true;
				String message		=
						"TimeTraveler: This computer can travel back in time. "
								+ "The high precision clock on this computer can move backwards. "
								+ "This will have a negative impact on counters and other high-precision measurements. "
								+ "Please have the high precision clock of this computer set "
								+ "appropriately for a multi-core environment.";
				getLogger().error(message);
				return;
			}
			timeNanoSav = timeNano;
		}

		getLogger().info("TimeTraveler: Your computer cannot travel back in time.");
		s_isTimeTraveler = false;
	}
}
