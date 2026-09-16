package com.lc.offgrid.pingapp;

import com.lc.basics.container.AbstractAction;
import com.lc.basics.container.AbstractContainer;
import com.lc.basics.tools.misc.BasicTools;

import java.io.File;
import java.util.List;

public enum PingAction implements AbstractAction<PingContainer>
{
	Monitor
			{
				@Override
				public void execute(PingContainer container) throws Exception
				{
					PingInterval	pingInterval	= AbstractContainer.queryEnum(
							"Interval?", "", PingInterval.class, PingInterval.Min_1);
					PingMonitor		monitor			= new PingMonitor(
							container.getPingFileManager(), pingInterval);

					AbstractContainer.timeStamp("Pinging one host every %,d seconds. Press Return to stop.",
							pingInterval.getSecondPerPass());
					monitor.run();
				}
			},

	Statistics
			{
				@Override
				public void execute(PingContainer container) throws Exception
				{
					PingStatistics		statistics		= new PingStatistics(container.getPingFileManager());
					statistics.report();
				}
			},

	Configuration
			{
				@Override
				public void execute(PingContainer container) throws Exception
				{
					PingFileManager		fileManager		= container.getPingFileManager();
					File				folder			= fileManager.getPingFolder();
					File				hostListFile	= fileManager.getHostListFile();
					File				pingLogFile		= fileManager.getPingLogFile();
					List<String>		hostList		= fileManager.readHostList();

					AbstractContainer.timeStamp("Running on %s", BasicTools.getComputerName());
					AbstractContainer.timeStamp("Folder %s", folder.getAbsolutePath());
					AbstractContainer.timeStamp("Pinging %,d hosts from %s",
							hostList.size(), hostListFile.getName());
					AbstractContainer.timeStamp("Logging to %s", pingLogFile.getName());
				}
			},

	Exit
			{
				@Override
				public void execute(PingContainer container) throws Exception
				{
					// Never called. runActionList() returns on the name before it executes.
					AbstractContainer.timeStamp("Goodbye.");
				}
			};
}
