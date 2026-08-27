package com.lc.offgrid.misc.files;

import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

public abstract class AbstractFileManager implements InitializingBean
{
	private final Map<String, File> nameMap = new TreeMap<>();

	abstract public String getRootFolder();
	public boolean isValid(File file)
	{
		return true;
	}

	public Map<String, File> getNameMap()
	{
		return nameMap;
	}

	public File getFile(String name)
	{
		return nameMap.get(name);
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		initNameMap(new File(getRootFolder()));
	}

	private void initNameMap(File file)
	{
		if (file.isFile())
		{
			if (isValid(file))
			{
				String filename = file.getName();
				String name = filename.substring(0, filename.lastIndexOf('.'));
				nameMap.put(name, file);
			}
		}
		else // isDirectory
		{
			File[] children = file.listFiles();
			if (null != children)
			{
				for (File child : children)
				{
					initNameMap(child);
				}
			}
		}
	}
}
