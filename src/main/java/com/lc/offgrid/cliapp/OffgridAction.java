package com.lc.offgrid.cliapp;


import com.lc.basics.container.AbstractAction;
import com.lc.basics.container.AbstractContainer;
import com.lc.basics.tools.misc.BasicRuntimeException;
import com.lc.basics.tools.misc.BasicTools;
import com.lc.offgrid.common.misc.imaging.ImageCompressor;
import com.lc.offgrid.common.misc.imaging.ImageMetadata;
import com.lc.offgrid.common.misc.imaging.ImageMetadataExtractor;
import com.lc.offgrid.common.misc.imaging.ImageSize;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.Map;
import java.util.function.Function;

public enum OffgridAction implements AbstractAction<OffgridContainer>
{
	ValidateImage
			{
				@Override
				public void execute(OffgridContainer container)
				{
					OffgridAction.processAllImages(container, file ->
					{
						String name = file.getAbsolutePath().substring(file.getAbsolutePath().indexOf("offgrid/images"));
						ImageMetadata metadata = ImageMetadataExtractor.getImageMetadata(file);
						if (metadata == null)
						{
							System.out.printf("Unknown type for %s\n", name);
							return false;
						}
						if (!metadata.isLegal())
						{
							System.out.printf("Mismatched types: mediaType(%s) != detected(%s) for %s\n",
									metadata.getMediaType(), metadata.getDetectedType(), name);
							return false;
						}
						try
						{
							ImageCompressor compressor = new ImageCompressor();
							compressor.open(file);
							compressor.modify(100_000, 100_000);
							compressor.toResource(0.75);
						}
						catch (BasicRuntimeException e)
						{
							System.out.printf("*** Failed to process image file: %s\n%s\n",name, e.getMessageChain());
						}
						catch (Exception e)
						{
							System.out.printf("*** Failed to process image file: %s\n%s\n", name, e.getMessage());
						}
						return true;
					});
				}
			},

	ReduceImage
			{
				@Override
				public void execute(OffgridContainer container) throws Exception
				{
					ImageSize size = queryImageSize();
					double quality = queryQuality();

					OffgridAction.processAllImages(container, fileFrom ->
					{
						ImageMetadata metadata = ImageMetadataExtractor.getImageMetadata(fileFrom);
						if (null == metadata || !metadata.isLegal())
						{
							return false;
						}

						File fileTo = size.makeFileTo(fileFrom);
						boolean exists = fileTo.exists();
						if (!exists)
						{
							size.resize(fileFrom, quality);
						}
						return !exists;
					});
				}
			},
	Where
			{
				@Override
				public void execute(OffgridContainer container) throws Exception
				{
					// Never called, Exits without calling execute.
					OffgridContainer.timeStamp("Running on %s", BasicTools.getComputerName());
				}
			},
	Exit
			{
				@Override
				public void execute(OffgridContainer container) throws Exception
				{
					// Never called, Exits without calling execute.
					OffgridContainer.timeStamp("Goodbye.");
				}
			};

	private static double queryQuality() throws Exception
	{
		return AbstractContainer.queryDouble("Quality level?", "", 0.75);
	}

	private static ImageSize queryImageSize() throws Exception
	{
		return AbstractContainer.queryEnum("Image size?", "", ImageSize.class, ImageSize.Small);
	}

	private static void processAllImages(OffgridContainer container, Function<File, Boolean> fn)
	{
		Map<String, Resource> map = container.getImageFileManager().getNameMap();
		int count = 0;
		int countProcessed = 0;
		int countMax = map.size();
		for (Map.Entry<String, Resource> entry : map.entrySet())
		{
			count++;
			Resource resource = entry.getValue();
			if (1 == count || 0 == (count % 25))
			{
				AbstractContainer.timeStamp("Processing image %3d of %3d: %s",
						count, countMax, entry.getKey());
			}
			try
			{
				File file = resource.getFile();
				if (!file.getAbsolutePath().contains("native"))
				{
					continue;
				}

				boolean isProcessed = fn.apply(file);
				if  (isProcessed)
				{
					countProcessed++;
				}
			}
			catch (Exception e)
			{
				AbstractContainer.timeStamp("Failed to open image file: %s\nWith %s",
						resource.getDescription(), e.getMessage());
			}
			if (count > 1_000_000)
			{
				break;
			}
		}
		AbstractContainer.timeStamp("Processed %,d of %,d images", countProcessed, countMax);
	}
}
