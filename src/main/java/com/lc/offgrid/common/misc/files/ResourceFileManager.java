package com.lc.offgrid.common.misc.files;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

// Resources are on the classpath: a folder when run from the IDE, entries in the jar when deployed
public class ResourceFileManager extends AbstractFileManager
{
	private String folderName;
	private String filePattern;

	@Component
	public static class JsonResourceFileManager extends ResourceFileManager
	{
		public JsonResourceFileManager()
		{
			super("/data", "*.json");
		}
	}

	public ResourceFileManager(String folderName, String filePattern)
	{
		this.folderName = folderName;
		this.filePattern = filePattern;
	}

	@Override
	public String getRootFolder()
	{
		return folderName;
	}

	public String getFilePattern()
	{
		return filePattern;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		String locationPattern = String.format("classpath:%s/**/%s", getRootFolder(), getFilePattern());
		Resource[] resourceList = resolver.getResources(locationPattern);
		for (Resource resource : resourceList)
		{
			addResource(resource);
		}
	}
}
