package com.lc.offgrid.pojo.external;

import com.lc.offgrid.pojo.external.bc.offramp.OfframpFile;
import com.lc.offgrid.pojo.external.bc.reststop.RestStopFile;
import com.lc.offgrid.pojo.external.overpass.amenities.AmenityFile;
import com.lc.offgrid.pojo.external.overpass.exits.ExitFile;
import com.lc.offgrid.spring.tools.BaseWebProcessor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * Access to the downloads held under resources/external/download. Each one is read once, at
 * startup, and handed out from here.
 */
@Component
public class ExternalManager implements InitializingBean
{
	private RestStopFile	restStopFile;
	private ExitFile		exitFile;
	private OfframpFile		offrampFile;
	private AmenityFile		amenityFile;

	public RestStopFile getRestStopFile()
	{
		return restStopFile;
	}

	public ExitFile getExitFile()
	{
		return exitFile;
	}

	public OfframpFile getOfframpFile()
	{
		return offrampFile;
	}

	public AmenityFile getAmenityFile()
	{
		return amenityFile;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		String restStopPath = ExternalUpdater.getRestStopPath();
		restStopFile = BaseWebProcessor.readFile(restStopPath, RestStopFile.class);

		String exitPath = ExternalUpdater.getExitPath();
		exitFile = BaseWebProcessor.readFile(exitPath, ExitFile.class);

		String offrampPath = ExternalUpdater.getOfframpPath();
		offrampFile = BaseWebProcessor.readFile(offrampPath, OfframpFile.class);

		String amenityPath = ExternalUpdater.getAmenityPath();
		amenityFile = BaseWebProcessor.readFile(amenityPath, AmenityFile.class);
	}
}
