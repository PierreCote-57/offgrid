/*
 * Copyright (c) 2016 LogicielCote.COM Systems All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.monitoring.statistics;

public interface Accumulator
{
	void			record(double value);
	void 			record(Accumulator accumulator);

	Accumulator		getBlankClone();
	void			reset();
	Accumulator		cloneAndReset();
	void			cloneAndReset(Accumulator accumulator);

	long			getCount();
	double			getMin();
	double			getMax();
	double			getAvg();
	double			getStd();
	double			getSum();
}
