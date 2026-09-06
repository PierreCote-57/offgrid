package com.lc.offgrid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Serves the site. Scans what the site needs and nothing the CLI owns.
@SpringBootApplication(scanBasePackages = {"com.lc.offgrid.common", "com.lc.offgrid.webapp"})
public class OffgridApplicationWeb
{
	public static void main(String[] args)
	{
		SpringApplication.run(OffgridApplicationWeb.class, args);
	}
}
