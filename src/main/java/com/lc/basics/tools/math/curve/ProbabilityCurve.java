/*
 * Copyright (c) 2016 LogicielCote.COM All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.math.curve;

import java.io.Serializable;

/**
 * Created by Pierre on 2/17/16.
 */
public interface ProbabilityCurve extends Serializable, Curve
{
	double getMean();
	double getCumulative(double x);
}
