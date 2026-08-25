/*
 * Copyright (c) 2021 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.function;

public interface TriFunction<X, Y, Z, A>
{
	A apply(X x, Y y, Z z);
}
