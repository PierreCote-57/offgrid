/*
 * Copyright (c) 2026 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.offgrid.misc.imaging;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;

/**
 * The size a caller asks an image for, and the box that size means. The name rides as the `size`
 * query parameter on /image/{imageName}, written by a page or by a script rather than by a
 * person, so a name this enum does not carry is answered as Native rather than as a refusal.
 *
 * The box is a ceiling the image fits inside, keeping its aspect ratio — only one of the two
 * numbers binds, the width for a landscape shot and the height for a portrait one. Native's box
 * is past any real photograph, so nothing is ever scaled down to it.
 *
 * @author Pierre
 */
public enum ImageSize
{
	Small(640, 640),
	Medium(1920, 1440),
	Large(2560, 1920),
	Native(10_000, 10_000)
	{
		/** The file itself, which is what this size means — a copy would be a re-encoding of it. */
		@Override
		public File resize(File fileFrom, double quality)
		{
			File answer = fileFrom;
			return answer;
		}

		/** The file's own bytes, read as they are rather than decoded and written again. */
		@Override
		public Resource toResource(File fileFrom, double quality)
		{
			Resource answer = new FileSystemResource(fileFrom);
			return answer;
		}
	};

	private final int width;
	private final int height;

	ImageSize(int width, int height)
	{
		this.width = width;
		this.height = height;
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

	/**
	 * A copy of one image file at this size, written to a peer folder named for the size and
	 * carrying that name as a suffix — /images/van/IMG_1234.jpg at Small becomes
	 * /images/small/IMG_1234-small.jpg. Answers the file written.
	 */
	public File resize(File fileFrom, double quality)
	{
		String			folder		= name().toLowerCase();
		ImageCompressor	compressor	= new ImageCompressor();

		compressor.open(fileFrom);
		compressor.modify(getWidth(), getHeight());

		File answer = compressor.write(folder, folder, quality);
		return answer;
	}

	/**
	 * One image file at this size, as JPEG bytes held in memory. The file on disk is left alone.
	 */
	public Resource toResource(File fileFrom, double quality)
	{
		ImageCompressor compressor = new ImageCompressor();

		compressor.open(fileFrom);
		compressor.modify(getWidth(), getHeight());

		Resource answer = compressor.toResource(quality);
		return answer;
	}

	/**
	 * The size a request names, Native when it names nothing or names something this enum does
	 * not have. The match ignores case, so a URL may write the name however it reads best.
	 */
	public static ImageSize of(String name)
	{
		if (null == name)
		{
			return Native;
		}

		for (ImageSize size : values())
		{
			if (size.name().equalsIgnoreCase(name))
			{
				return size;
			}
		}

		return Native;
	}
}
