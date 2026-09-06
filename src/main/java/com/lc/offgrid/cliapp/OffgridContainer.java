package com.lc.offgrid.cliapp;

import com.lc.basics.container.AbstractContainer;
import com.lc.offgrid.common.misc.files.LocalFileManager.*;
import com.lc.offgrid.common.misc.files.ResourceFileManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// The work a command-line run performs, once the context is up.
@Component
public class OffgridContainer extends AbstractContainer
{

	@Autowired
	private DocumentFileManager documentFileManager;

	@Autowired
	private ImageFileManager imageFileManager;

	@Autowired
	private ResourceFileManager.JsonResourceFileManager jsonResourceFileManager;

	public DocumentFileManager getDocumentFileManager()
	{
		return documentFileManager;
	}
	public ImageFileManager getImageFileManager()
	{
		return imageFileManager;
	}
	public ResourceFileManager.JsonResourceFileManager getJsonResourceFileManager()
	{
		return jsonResourceFileManager;
	}

	public void execute()
	{
		runActionList(OffgridAction.values(), () -> "Select a Offgrid action");
	}
}
