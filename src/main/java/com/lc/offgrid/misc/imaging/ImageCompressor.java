/*
 * Copyright (c) 2026 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.offgrid.misc.imaging;

import com.lc.basics.tools.misc.BasicRuntimeException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Iterator;

/**
 * Makes a smaller copy of one image. The image being worked on is held as state, so the three
 * steps — open, modify, toResource — can be called one at a time, and resize is those three in
 * order.
 *
 * @author Pierre
 */
public class ImageCompressor
{
	private BufferedImage image;

	public BufferedImage getImage()
	{
		return image;
	}

	public void setImage(BufferedImage image)
	{
		this.image = image;
	}

	/**
	 * A copy of one image file, scaled to fit inside the box and written as a JPEG at that
	 * quality.
	 */
	public Resource resize(File fileFrom, int boxWidth, int boxHeight, double quality)
	{
		open(fileFrom);
		modify(boxWidth, boxHeight);

		Resource answer = toResource(quality);
		return answer;
	}

	/**
	 * The pixels of one image file, which become the image this compressor is working on. A file
	 * that cannot be read as an image throws.
	 */
	public BufferedImage open(File fileFrom)
	{
		BufferedImage openedImage;

		try
		{
			openedImage = ImageIO.read(fileFrom);
		}
		catch (Exception e)
		{
			throw new BasicRuntimeException(e, "Error reading image: %s", fileFrom);
		}

		if (null == openedImage)
		{
			throw new BasicRuntimeException("Unable to read image: %s", fileFrom);
		}

		setImage(openedImage);
		return openedImage;
	}

	/**
	 * The image scaled to fit inside the box, keeping its aspect ratio. Only one of the two
	 * numbers binds — a landscape shot is held by the width, a portrait one by the height. An
	 * image already inside the box is left as it is, since enlarging it adds pixels the file
	 * never carried.
	 */
	public BufferedImage modify(int boxWidth, int boxHeight)
	{
		BufferedImage	sourceImage		= getImage();
		int				sourceWidth		= sourceImage.getWidth();
		int				sourceHeight	= sourceImage.getHeight();
		double			widthScale		= (double) boxWidth / (double) sourceWidth;
		double			heightScale		= (double) boxHeight / (double) sourceHeight;
		double			scale			= Math.min(widthScale, heightScale);

		if (scale >= 1.0)
		{
			return sourceImage;
		}

		int				targetWidth		= (int) Math.round(sourceWidth * scale);
		int				targetHeight	= (int) Math.round(sourceHeight * scale);
		BufferedImage	targetImage		= new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D		graphics		= targetImage.createGraphics();

		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.drawImage(sourceImage, 0, 0, targetWidth, targetHeight, null);
		graphics.dispose();

		setImage(targetImage);
		return targetImage;
	}

	/**
	 * The image written out as JPEG bytes, at a quality between 0 and 1. The bytes are held in
	 * memory rather than on disk, so the caller decides whether they are answered, kept, or both.
	 */
	public Resource toResource(double quality)
	{
		ByteArrayOutputStream	byteStream	= new ByteArrayOutputStream();
		Iterator<ImageWriter>	writerList	= ImageIO.getImageWritersByFormatName("jpeg");
		ImageWriter				writer		= writerList.next();
		ImageWriteParam			writeParam	= writer.getDefaultWriteParam();

		writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		writeParam.setCompressionQuality((float) quality);

		try
		{
			ImageOutputStream	imageStream	= ImageIO.createImageOutputStream(byteStream);
			IIOImage			iioImage	= new IIOImage(getImage(), null, null);

			writer.setOutput(imageStream);
			writer.write(null, iioImage, writeParam);
			imageStream.close();
		}
		catch (Exception e)
		{
			throw new BasicRuntimeException(e, "Error writing image");
		}
		finally
		{
			writer.dispose();
		}

		byte[]	byteList	= byteStream.toByteArray();
		Resource answer		= new ByteArrayResource(byteList);
		return answer;
	}
}
