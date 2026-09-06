package com.lc.basics.container;

public interface AbstractAction<T extends AbstractContainer>
{
	void execute(T container) throws Exception;

	String name();
}
