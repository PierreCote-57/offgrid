package com.lc.offgrid.misc;

import com.lc.offgrid.AbstractTests;
import com.lc.offgrid.common.misc.files.AbstractFileManager;
import com.lc.offgrid.common.misc.files.ResourceFileManager;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.io.Resource;

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
		String filePattern = String.format("*.%s", ext);
		AbstractFileManager fileMmanager = new ResourceFileManager("/data", filePattern);
		fileMmanager.afterPropertiesSet();

		Map<String, Resource> map = fileMmanager.getNameMap();
		assertNotNull(map);
		assertFalse(map.isEmpty());
	}
}
