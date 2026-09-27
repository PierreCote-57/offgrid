package com.lc.offgrid.pingapp;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Runs the ping menu and exits. The scan covers this package only — the app reads its own
 * folder and needs none of the site's beans. The web type comes from the classpath, so NONE
 * is what keeps Tomcat from starting.
 */
@SpringBootApplication(scanBasePackages = {"com.lc.offgrid.pingapp"})
public class OffgridApplicationPing
{
	public static void main(String[] args)
	{
		SpringApplicationBuilder		builder		= new SpringApplicationBuilder(OffgridApplicationPing.class);
		builder.web(WebApplicationType.NONE);
		builder.profiles("dev");

		ConfigurableApplicationContext	context		= builder.run(args);
		PingContainer					command		= context.getBean(PingContainer.class);
		command.execute();
		context.close();
	}
}
