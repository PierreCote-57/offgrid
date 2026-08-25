/*
 * Copyright (c) 2021 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.function;

public interface BiFunctionWithException<T, U, R>
{
	/**
	 * Applies this function to the given arguments.
	 *
	 * @param t the first function argument
	 * @param u the second function argument
	 * @return the function result
	 */
	R apply(T t, U u) throws Exception;
}
