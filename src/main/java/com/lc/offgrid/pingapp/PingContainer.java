package com.lc.offgrid.pingapp;

import com.lc.basics.container.AbstractContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// The work a ping run performs, once the context is up.
@Component
public class PingContainer extends AbstractContainer
{
	@Autowired
	private PingFileManager pingFileManager;

	public PingFileManager getPingFileManager()
	{
		return pingFileManager;
	}

	public void execute()
	{
		runActionList(PingAction.values(), () -> "Select a Ping action");
	}
}
