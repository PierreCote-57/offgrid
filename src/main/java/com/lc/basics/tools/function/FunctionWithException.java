/*
 * Copyright (c) 2021 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.function;

public interface FunctionWithException<T, R>
{
	/**
	 * Applies this function to the given arguments.
	 *
	 * @param t the first function argument
	 * @return the function result
	 */
	R apply(T t) throws Exception;
}
