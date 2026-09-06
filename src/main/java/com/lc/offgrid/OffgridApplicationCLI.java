package com.lc.offgrid;

import com.lc.offgrid.cliapp.OffgridCommand;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

// Runs a command and exits. The web type comes from the classpath, so NONE is what keeps
// Tomcat from starting; with it, nothing holds a thread and the JVM ends when main returns.
@SpringBootApplication(scanBasePackages = {"com.lc.offgrid.common", "com.lc.offgrid.cliapp"})
public class OffgridApplicationCLI
{
	public static void main(String[] args)
	{
		SpringApplicationBuilder		builder		= new SpringApplicationBuilder(OffgridApplicationCLI.class);
		builder.web(WebApplicationType.NONE);
		builder.profiles("local");

		ConfigurableApplicationContext	context		= builder.run(args);
		OffgridCommand					command		= context.getBean(OffgridCommand.class);
		command.execute();
		context.close();
	}
}
