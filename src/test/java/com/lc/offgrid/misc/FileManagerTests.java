package com.lc.offgrid.misc;

import com.lc.offgrid.AbstractTests;
import com.lc.offgrid.misc.files.AbstractFileManager;
import com.lc.offgrid.misc.files.ResourceFileManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FileManagerTests extends AbstractTests
{
	public static Object[][] ResourceFileData()
	{
		return new Object[][]{
				{"json"},
		};
	}

	@ParameterizedTest
	@MethodSource("ResourceFileData")
	public void testImageFiles(String ext) throws Exception
	{
		AbstractFileManager fileMmanager = new ResourceFileManager(ext);
		fileMmanager.afterPropertiesSet();

		Map<String, File> map = fileMmanager.getNameMap();
		assertNotNull(map);
		assertFalse(map.isEmpty());
	}
}
