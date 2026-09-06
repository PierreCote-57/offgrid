package com.lc.offgrid.cliapp;

import com.lc.basics.tools.logging.BasicLogger;
import org.springframework.stereotype.Component;

// The work a command-line run performs, once the context is up.
@Component
public class OffgridCommand
{
	private static final BasicLogger LOGGER = BasicLogger.getLogger(OffgridCommand.class);

	public void execute()
	{
		LOGGER.info("Hello");
	}
}
