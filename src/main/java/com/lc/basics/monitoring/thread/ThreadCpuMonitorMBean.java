/*
 * Copyright (c) 2016 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */
package com.lc.basics.monitoring.thread;

import java.util.Date;
import java.util.Map;

public interface ThreadCpuMonitorMBean
{
	long getLastUpdateTime();
	Date getLastUpdateDate();

	double					getThresholdPct();
	void					setThresholdPct(double thresholdPct);
	Map<String, String>		getCpuMap();
}
