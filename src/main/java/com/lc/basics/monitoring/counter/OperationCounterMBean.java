/*
 * Copyright (c) 2016 LogicielCote.COM Systems All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.monitoring.counter;

public interface OperationCounterMBean
{
	long		getCountBegin();
	long		getCountSuccess();
	long		getCountFailure();
	long		getCountOutstanding();

	double		getRate();

	double		getDurationCount();
	double		getDurationMinMS();
	double		getDurationAvgMS();
	double		getDurationMaxMS();
	double		getDurationStdMS();
}
