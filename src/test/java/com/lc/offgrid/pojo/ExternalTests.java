package com.lc.offgrid.pojo;

import com.lc.offgrid.AbstractTests;
import com.lc.offgrid.common.misc.external.ExternalManager;
import com.lc.offgrid.common.pojo.external.bc.offramp.OfframpFeature;
import com.lc.offgrid.common.pojo.external.bc.offramp.OfframpFile;
import com.lc.offgrid.common.pojo.external.bc.reststop.RestStopFeature;
import com.lc.offgrid.common.pojo.external.bc.reststop.RestStopFile;
import com.lc.offgrid.common.pojo.external.overpass.amenities.AmenityElement;
import com.lc.offgrid.common.pojo.external.overpass.amenities.AmenityFile;
import com.lc.offgrid.common.pojo.external.overpass.exits.ExitElement;
import com.lc.offgrid.common.pojo.external.overpass.exits.ExitFile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ExternalTests extends AbstractTests
{
	@Test
	public void testRestStopFile() throws Exception
	{
		ExternalManager manager = new ExternalManager();
		manager.afterPropertiesSet();
		RestStopFile restStopFile = manager.getRestStopFile();
		List<RestStopFeature> list = restStopFile.getFeatureList();
		assertNotNull(list);
		assertFalse(list.isEmpty());
	}

	@Test
	public void testExitFile() throws Exception
	{
		ExternalManager manager = new ExternalManager();
		manager.afterPropertiesSet();
		ExitFile exitFile = manager.getExitFile();
		List<ExitElement> list = exitFile.getElementList();
		assertNotNull(list);
		assertFalse(list.isEmpty());
	}

	@Test
	public void testOfframpFile() throws Exception
	{
		ExternalManager manager = new ExternalManager();
		manager.afterPropertiesSet();
		OfframpFile offrampFile = manager.getOfframpFile();
		List<OfframpFeature> list = offrampFile.getFeatureList();
		assertNotNull(list);
		assertFalse(list.isEmpty());
	}

	@Test
	public void testAmenityFile() throws Exception
	{
		ExternalManager manager = new ExternalManager();
		manager.afterPropertiesSet();
		AmenityFile amenityFile = manager.getAmenityFile();
		List<AmenityElement> list = amenityFile.getElementList();
		assertNotNull(list);
		assertFalse(list.isEmpty());
	}
}
