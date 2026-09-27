package com.lc.offgrid.common.misc.files;

import com.lc.basics.tools.file.ClassFinder;
import com.lc.basics.tools.file.SimpleFilterString;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.TreeMap;

public abstract class AbstractFileManager implements InitializingBean
{
	private final Map<String, Resource> nameMap = new TreeMap<>();

	abstract public String getRootFolder();

	public Map<String, Resource> getNameMap()
	{
		return nameMap;
	}

	public Map<String, Resource> filterMap(SimpleFilterString filter)
	{
		Map<String, Resource> map = new TreeMap<>();
		for (Map.Entry<String, Resource> entry : getNameMap().entrySet())
		{
			String name = entry.getKey();
			Resource resource = entry.getValue();
			String path = getPath(resource);
			if (filter.accept(path))
			{
				map.put(name, resource);
			}
		}
		return map;
	}

	public Resource getResource(String name)
	{
		name = name.contains("/")
				? name.substring(name.lastIndexOf("/")+1)
				: name;
		name = name.toLowerCase();
		return getNameMap().get(name);
	}

	// Null when not found. Throws for a resource that is not on disk, such as a jar entry.
	public File getFile(String name) throws IOException
	{
		Resource resource = getResource(name);
		File file = null == resource
				? null
				: resource.getFile();
		return file;
	}

	// The name is the filename, lowercased, without its extension. A file with no extension is skipped.
	protected void addResource(Resource resource)
	{
		String filename = resource.getFilename();
		if (null != filename && filename.contains("."))
		{
			String name = filename.substring(0, filename.lastIndexOf('.'));
			name = name.toLowerCase();
			getNameMap().put(name, resource);
		}
	}

	// A path to match against: the absolute path on disk, or the jar path ending in the entry.
	public static String getPath(Resource resource)
	{
		try
		{
			String path = resource.getURL().getPath();
			return path;
		}
		catch (IOException e)
		{
			String message = String.format("getPath(%s)", resource.getDescription());
			throw new UncheckedIOException(message, e);
		}
	}
}
