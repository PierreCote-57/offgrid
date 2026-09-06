package com.lc.offgrid.common.misc.external;

import com.lc.basics.tools.file.BaseFileHandler;
import com.lc.offgrid.common.pojo.external.bc.offramp.OfframpFile;
import com.lc.offgrid.common.pojo.external.bc.reststop.RestStopFile;
import com.lc.offgrid.common.pojo.external.overpass.amenities.AmenityFile;
import com.lc.offgrid.common.pojo.external.overpass.exits.ExitFile;
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
		restStopFile = BaseFileHandler.readFile(restStopPath, RestStopFile.class);

		String exitPath = ExternalUpdater.getExitPath();
		exitFile = BaseFileHandler.readFile(exitPath, ExitFile.class);

		String offrampPath = ExternalUpdater.getOfframpPath();
		offrampFile = BaseFileHandler.readFile(offrampPath, OfframpFile.class);

		String amenityPath = ExternalUpdater.getAmenityPath();
		amenityFile = BaseFileHandler.readFile(amenityPath, AmenityFile.class);
	}
}
