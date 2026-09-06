package com.lc.offgrid;

import org.springframework.boot.autoconfigure.SpringBootApplication;

// The context a test runs against: every bean of the three roots, whichever launcher would
// have owned it in production.
@SpringBootApplication(scanBasePackages = {"com.lc.offgrid.common", "com.lc.offgrid.webapp", "com.lc.offgrid.cliapp"})
public class OffgridTestApplication
{
}
