/*
 * Copyright (c) 2026 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.offgrid.misc.imaging;

/**
 * The size a caller asks an image for. The name rides as the `size` query parameter on
 * /image/{imageName}, written by a page or by a script rather than by a person, so a name this
 * enum does not carry is answered as null rather than as a refusal.
 *
 * @author Pierre
 */
public enum ImageSize
{
	Small,
	Medium,
	Large;

	/**
	 * The size a request names, null when it names nothing or names something this enum does
	 * not have. The match ignores case, so a URL may write the name however it reads best.
	 */
	public static ImageSize of(String name)
	{
		if (null == name)
		{
			return null;
		}

		for (ImageSize size : values())
		{
			if (size.name().equalsIgnoreCase(name))
			{
				return size;
			}
		}

		return null;
	}
}
