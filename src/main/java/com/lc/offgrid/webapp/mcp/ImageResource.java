package com.lc.offgrid.webapp.mcp;

import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.stereotype.Component;

/**
 * The images this server hands to a client, published as MCP resources.
 */
@Component
public class ImageResource extends AbstractOffgridMCP
{
	/**
	 * A one pixel JPEG, standing in for a photo until the image folder is read. It is base64
	 * because the resource declares an image mimeType, which makes what it returns a blob.
	 */
	private static final String SAMPLE_JPEG = """
			/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDABsSFBcUERsXFhceHBsgKEIrKCUlKFE6PTBCYFVlZF9VXVtqeJmBanG\
			Qc1tdhbWGkJ6jq62rZ4C8ybqmx5moq6T/2wBDARweHigjKE4rK06kbl1upKSkpKSkpKSkpKSkpKSkpKSkpKSkpK\
			SkpKSkpKSkpKSkpKSkpKSkpKSkpKSkpKSkpKT/wAARCAABAAEDASIAAhEBAxEB/8QAFAABAAAAAAAAAAAAAAAAA\
			AAAAP/EABQQAQAAAAAAAAAAAAAAAAAAAAD/xAAUAQEAAAAAAAAAAAAAAAAAAAAD/8QAFBEBAAAAAAAAAAAAAAAA\
			AAAAAP/aAAwDAQACEQMRAD8AAAK//9k=""";

	/**
	 * The images this server can hand out, one file name per line.
	 * <p>
	 * requestUri is unused and cannot be dropped: a resource whose uri holds no variable must
	 * still declare a String or a ReadResourceRequest parameter, and a String receives the uri
	 * the client asked for.
	 */
	@McpResource(uri = "offgrid://image", name = "image-list",
			description = "The images this site can hand you, one file name per line",
			mimeType = "text/plain")
	public String readImageList(String requestUri)
	{
		logMcpCall("readImageList(%s)", requestUri);

		String imageNameList = "rathtrevor-beach.jpg\nmohun-lake.jpg\namor-lake.jpg";
		return imageNameList;
	}

	/**
	 * One image, named by a file name off that list. Spring AI matches the read uri against the
	 * template and hands the {fileName} it found to the parameter.
	 */
	@McpResource(uri = "offgrid://image/{fileName}", name = "image",
			description = "One image, by the file name the image list gives",
			mimeType = "image/jpeg")
	public String readImage(String fileName)
	{
		logMcpCall("readImage(%s)", fileName);

		String base64Image = SAMPLE_JPEG;
		return base64Image;
	}
}
