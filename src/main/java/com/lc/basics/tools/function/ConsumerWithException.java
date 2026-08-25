/*
 * Copyright (c) 2020 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.function;

public interface ConsumerWithException<T>
{
	void accept(T t) throws Exception;
}
