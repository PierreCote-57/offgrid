/*
 * Copyright (c) 2016 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */
package com.lc.basics.monitoring.garbage;

import java.util.Date;

public interface GarbageCollectorItemMonitorMBean
{
	long getMeasureTimeMS();
	Date getMeasureDate();

	String getName();
	double getPeriodSec();
	double getDurationAvgMS();
	double getDutyCyclePct();

	void setName(String name);
	void reset(String reason);
}
