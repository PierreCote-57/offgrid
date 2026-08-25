/*
 * Copyright (c) 2020 LogicielCote.COM Systems All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.thread;

public interface BasicTimerTaskMBean
{
	String getName();
	long getPeriodMS();
	long getOffsetMS();
	long getLastTimeMS();
	long getLastDateMS();
	long getLastDurationUS();

	void stop();
}
