package com.lc.offgrid.misc;

import com.lc.basics.tools.function.TriFunction;
import com.lc.offgrid.AbstractTests;
import com.lc.offgrid.OffgridTestApplication;
import com.lc.offgrid.common.misc.files.AbstractFileManager;
import com.lc.offgrid.common.misc.files.LocalFileManager.DocumentFileManager;
import com.lc.offgrid.common.misc.files.LocalFileManager.ImageFileManager;
import com.lc.offgrid.common.misc.files.ResourceFileManager.JsonResourceFileManager;
import com.lc.offgrid.common.misc.imaging.ImageSize;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The file managers taken from the Spring context rather than constructed: the root folder is
 * the one the profile states, and afterPropertiesSet has already run on each bean.
 */
@SpringBootTest(classes = OffgridTestApplication.class)
@ActiveProfiles("local")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ResourceWithSpringTests extends AbstractTests
{
	@Autowired
	private DocumentFileManager documentFileManager;

	@Autowired
	private ImageFileManager imageFileManager;

	@Autowired
	private JsonResourceFileManager jsonResourceFileManager;

	public DocumentFileManager getDocumentFileManager()
	{
		return documentFileManager;
	}
	public ImageFileManager getImageFileManager()
	{
		return imageFileManager;
	}
	public JsonResourceFileManager getJsonResourceFileManager()
	{
		return jsonResourceFileManager;
	}

	// Not static: it reads the injected beans, which exist because the instance is per class.
	public Object[][] FileManagerSource()
	{
		return new Object[][] {
				new Object[] {getDocumentFileManager()},
				new Object[] {getImageFileManager()},
				new Object[] {getJsonResourceFileManager()},
		};
	}

	@ParameterizedTest
	@MethodSource("FileManagerSource")
	public void testFileManager(AbstractFileManager fileManager)
	{
		assertNotNull(fileManager);

		String rootFolder = fileManager.getRootFolder();
		assertNotNull(rootFolder);

		Map<String, File> nameMap = fileManager.getNameMap();
		assertNotNull(nameMap);
		assertFalse(nameMap.isEmpty(), String.format("No file found under %s", rootFolder));
	}


	public Object[][] ImageResizeSource()
	{
		TriFunction<ImageSize, File, Double, Object> fn1 =
				ImageSize::resize;
		TriFunction<ImageSize, File, Double, Object> fn2 =
				ImageSize::resize;
		TriFunction<ImageSize, File, Double, Object>[] fnList =
				new TriFunction[] {fn1, fn2};

		List<Object[]> objectList = new ArrayList<>();
		for (ImageSize size : ImageSize.values())
		{
			for (String fnName : new String[] {"resize", "toResource"})
			{
				objectList.add(new Object[] {fnName, size, 1.0, fn1});
			}
		}
		return objectList.toArray(new Object[0][]);
	}

	@ParameterizedTest
	@MethodSource("ImageResizeSource")
	public void testImageResize(String name,
			ImageSize size,
			double quality,
			TriFunction<ImageSize, File, Double, Object> function)
	{
		int count = 10;
		List<File> list = makeFileList(getImageFileManager(), count, "2021");
		assertEquals(count, list.size());

		long start = System.nanoTime();
		for (File file : list)
		{
			function.apply(size, file, quality);
		}
		long end = System.nanoTime();
		long ns = end - start;

		String message = String.format("%s.%s(%.2f)", size, name, quality);
		reportPerformance(message, ns, count, true);

	}
	private List<File> makeFileList(AbstractFileManager manager, int count, String contains)
	{
		Map<String, File> map = manager.getNameMap();
		List<File> fileList = new ArrayList<>(map.values());
		List<File> list = new ArrayList<>(count);
		while (list.size() < count)
		{
			File file = fileList.get(getRandom().nextInt(fileList.size()));
			if (null == contains || file.getAbsolutePath().contains(contains))
			{
				list.add(file);
			}
		}
		return list;
	}
}
