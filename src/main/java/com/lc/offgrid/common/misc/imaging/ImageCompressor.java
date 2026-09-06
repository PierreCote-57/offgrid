/*
 * Copyright (c) 2026 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.offgrid.common.misc.imaging;

import com.lc.basics.tools.file.BaseFileHandler;
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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;

/**
 * Makes a smaller copy of one image. The file and the pixels being worked on are held as state,
 * so the steps — open, modify, then toResource or write — can be called one at a time, and
 * resize is open, modify and toResource in order.
 *
 * One image at a time: a caller works it with its own instance rather than sharing one.
 *
 * @author Pierre
 */
public class ImageCompressor
{
	private File			fileFrom;
	private BufferedImage	image;

	public File getFileFrom()
	{
		return fileFrom;
	}

	public void setFileFrom(File fileFrom)
	{
		this.fileFrom = fileFrom;
	}

	public BufferedImage getImage()
	{
		return image;
	}

	public void setImage(BufferedImage image)
	{
		this.image = image;
	}

	/**
	 * A copy of one image file, scaled to fit inside the box and answered as JPEG bytes at that
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
	 * Reads the file and takes its pixels as the image being worked on. A file that cannot be
	 * read as an image throws.
	 */
	public void open(File fileFrom)
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

		setFileFrom(fileFrom);
		setImage(openedImage);
	}

	/**
	 * Scales the image to fit inside the box, keeping its aspect ratio. Only one of the two
	 * numbers binds — a landscape shot is held by the width, a portrait one by the height. An
	 * image already inside the box is left as it is, since enlarging it adds pixels the file
	 * never carried.
	 */
	public void modify(int boxWidth, int boxHeight)
	{
		BufferedImage	sourceImage		= getImage();
		int				sourceWidth		= sourceImage.getWidth();
		int				sourceHeight	= sourceImage.getHeight();
		double			widthScale		= (double) boxWidth / (double) sourceWidth;
		double			heightScale		= (double) boxHeight / (double) sourceHeight;
		double			scale			= Math.min(widthScale, heightScale);

		if (scale >= 1.0)
		{
			return;
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

		byte[]		byteList	= byteStream.toByteArray();
		Resource	answer		= new ByteArrayResource(byteList);
		return answer;
	}

	/**
	 * Writes the image to that file, at a quality between 0 and 1, replacing whatever was there.
	 * Answers the file written.
	 */
	public File write(File fileTo, double quality)
	{
		Resource resource = toResource(quality);

		try (InputStream inputStream = resource.getInputStream())
		{
			Files.copy(inputStream, fileTo.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		catch (Exception e)
		{
			throw new BasicRuntimeException(e, "Error writing image: %s", fileTo);
		}

		return fileTo;
	}

	/**
	 * Writes the image next to the file it was opened from, in a peer folder when one is named
	 * and with the suffix on the name when one is given. Answers the file written.
	 */
	public File write(String folder, String nameSuffix, double quality)
	{
		File	fileFrom	= getFileFrom();
		File	fileTo		= makeFileTo(fileFrom, folder, nameSuffix);
		String	pathTo		= fileTo.getPath();

		BaseFileHandler.ensureFolder(pathTo);

		File answer = write(fileTo, quality);
		return answer;
	}

	/**
	 * Where a derived copy of one file lands: in a peer folder when one is named, keeping the
	 * source's folder otherwise, and with the suffix hung off the name when one is given, keeping
	 * the source's name otherwise. /images/van/IMG_1234.jpg with "small" and "small" answers
	 * /images/small/IMG_1234-small.jpg. Naming neither answers the source file itself, which is
	 * not a copy, and throws.
	 */
	public static File makeFileTo(File fileFrom, String folder, String nameSuffix)
	{
		if (null == folder && null == nameSuffix)
		{
			throw new BasicRuntimeException("Neither a folder nor a name suffix: %s", fileFrom);
		}

		String	nameFrom		= fileFrom.getName();
		File	folderFrom		= fileFrom.getParentFile();
		File	folderTo		= null == folder
				? folderFrom
				: new File(folderFrom.getParentFile(), folder);
		String	nameTo			= null == nameSuffix
				? nameFrom
				: suffixedName(nameFrom, nameSuffix);

		File answer = new File(folderTo, nameTo);
		return answer;
	}

	/**
	 * One filename with the suffix hung off its name — IMG_1234.jpg and "small" make
	 * IMG_1234-small.jpg. A name carrying no extension keeps none.
	 */
	private static String suffixedName(String filename, String nameSuffix)
	{
		String	baseName	= BaseFileHandler.extractName(filename);
		String	extension	= BaseFileHandler.extractExtension(filename);
		String	answer		= null == extension
				? String.format("%1$s-%2$s", baseName, nameSuffix)
				: String.format("%1$s-%2$s.%3$s", baseName, nameSuffix, extension);
		return answer;
	}
}
