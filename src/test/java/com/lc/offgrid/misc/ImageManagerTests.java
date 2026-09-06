package com.lc.offgrid.misc;

import com.lc.offgrid.AbstractTests;
import com.lc.offgrid.common.misc.files.LocalFileManager.*;
import com.lc.offgrid.common.misc.geography.GeoComparator;
import com.lc.offgrid.common.misc.geography.point.LatLonPoint;
import com.lc.offgrid.common.misc.imaging.ImageMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ImageManagerTests extends AbstractTests
{
	@Test
	public void testImageLoad() throws Exception
	{
		ImageFileManager manager = new ImageFileManager();
		manager.afterPropertiesSet();

		Map<String, File> nameMap	= manager.getNameMap();
		assertNotNull(nameMap);
		assertFalse(nameMap.isEmpty());
	}

	public static Object[][] FileSource()
	{
		return new Object[][] {
				new Object[] {"awning-control-panel",	true},
				new Object[] {"DumpStation.jpeg",		true},
				new Object[] {"IMG_0390",				true},
		};
	}

	@ParameterizedTest
	@MethodSource("FileSource")
	public void testImageFile(
			String name, boolean expectSuccess) throws Exception
	{
		ImageFileManager manager = new ImageFileManager();
		manager.afterPropertiesSet();

		boolean isSuccess;
		try
		{
			File file = manager.getFile(name);
			isSuccess = true;
		}
		catch (Exception e)
		{
			isSuccess = false;
		}
		assertEquals(expectSuccess, isSuccess);
	}

	public static Object[][] MetadataSource()
	{
		return new Object[][] {
				new Object[] {"awning-control-panel",	true},
				new Object[] {"DumpStation.jpeg",		false},
				new Object[] {"IMG_0390",				true},
		};
	}

	@ParameterizedTest
	@MethodSource("MetadataSource")
	public void testImageMetadata(
			String name, boolean expectSuccess) throws Exception
	{
		ImageFileManager manager = new ImageFileManager();
		manager.afterPropertiesSet();

		boolean isSuccess;
		try
		{
			ImageMetadata metadata = manager.getImageMetadata(name);
			isSuccess = true;
		}
		catch (Exception e)
		{
			isSuccess = false;
		}
		assertEquals(expectSuccess, isSuccess);
	}

	@Test
	public void testLatLng() throws Exception
	{
		ImageFileManager manager = new ImageFileManager();
		manager.afterPropertiesSet();

		Map<String, File> nameMap	= manager.getNameMap();
		for (Map.Entry<String, File> entry : nameMap.entrySet())
		{
			String name = entry.getKey();
			File file = entry.getValue();
			if (!name.startsWith("IMG_")
				|| 8 != name.length())
			{
				continue;
			}
			ImageMetadata metadata = manager.getImageMetadata(name);
			Double lat = metadata.getLatitude();
			Double lng = metadata.getLongitude();
			assertNotNull(metadata);
			assertNotNull(lat, "No lat for " + name);
			assertNotNull(lng, "No lng for " + name);
		}
	}

	public static Object[][] DistanceSource()
	{
		return new Object[][]{
				{ "IMG_0390", "IMG_2773", 13 }
		};
	}

	@ParameterizedTest
	@MethodSource("DistanceSource")
	public void testImageDistance(
			String name1, String name2, double expectded) throws Exception
	{
		ImageFileManager manager = new ImageFileManager();
		manager.afterPropertiesSet();

		ImageMetadata metadata1 = manager.getImageMetadata(name1);
		ImageMetadata metadata2 = manager.getImageMetadata(name2);

		LatLonPoint point1 = metadata1.getGeoPoint();
		LatLonPoint point2 = metadata2.getGeoPoint();
		assertNotNull(point1);
		assertNotNull(point2);

		double distance = GeoComparator.getDistanceKM(point1, point2);
		assertEquals(expectded, distance, 1.0);
	}

	public static Object[][] ObjectDistanceSource()
	{
		return new Object[][]{
				{ 1, 2, 13 }
		};
	}

	@ParameterizedTest
	@MethodSource("ObjectDistanceSource")
	public void testObjectImageDistance(double lat, double lng, int expected)
	{

	}
}
