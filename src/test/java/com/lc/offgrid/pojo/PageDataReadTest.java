package com.lc.offgrid.pojo;

import com.google.gson.Gson;
import com.lc.offgrid.pojo.page.PageData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Reads every JSON file under the data folder into a PageData, one test case per file. The
 * files are read from the source tree rather than the classpath, so a file added in the IDE
 * is picked up without a build.
 * <p>
 * The list_browser files are skipped: their root is an array, not a page object.
 * <p>
 * Every file is read as the base PageData, so a lake's fishingReferences and a campsite's
 * access are not exercised here — whatever picks the subclass does not exist yet.
 */
class PageDataReadTest {

	private static final Path	DATA_FOLDER	= Path.of("src/main/resources/data");

	private static final Gson	GSON		= new Gson();

	/** Every JSON file under the data folder, in path order so the cases are stable. */
	static Stream<Path> jsonFileList() throws IOException {
		try (Stream<Path> dataFolderStream = Files.walk(DATA_FOLDER)) {
			List<Path> jsonFileList = dataFolderStream
					.filter(path -> path.toString().endsWith(".json"))
					.filter(path -> !path.toString().contains("list_browser"))
					.sorted()
					.toList();
			Stream<Path> jsonFileStream = jsonFileList.stream();
			return jsonFileStream;
		}
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("jsonFileList")
	void readsIntoPageData(Path jsonFile) throws IOException {
		try (Reader reader = Files.newBufferedReader(jsonFile)) {
			PageData pageData = GSON.fromJson(reader, PageData.class);
			assertNotNull(pageData);
			// assert* goes here.
		}
	}
}
