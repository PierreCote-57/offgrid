package com.lc.offgrid.misc.files;

import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class LocalFileManager extends AbstractFileManager
{
	private String rootPath;

	public LocalFileManager()
	{
		this(null);
	}

	public LocalFileManager(String rootPath)
	{
		this.rootPath = null == rootPath
				? super.getRootFolder() + "/Documents"
				: rootPath;
	}

	@Override
	public String getRootFolder()
	{
		return rootPath;
	}
}
