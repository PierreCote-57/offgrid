/*
 * Copyright (c) 2021 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.function;

public interface QuadFunction<X1, X2, X3, X4, A>
{
	A apply(X1 x1, X2 x2, X3 x3, X4 x4);
}
