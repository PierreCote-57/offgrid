/*
 * Copyright (c) 2017 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */
package com.lc.basics.tools.thread;

public interface MySemaphoreMBean
{
	int getCountMax();
	void setCountMax(int countMax);
	int getCountActive();
	int getCountWaiting();
}
