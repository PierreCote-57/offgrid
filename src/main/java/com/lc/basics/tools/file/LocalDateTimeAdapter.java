package com.lc.basics.tools.file;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * A LocalDateTime on the wire is the ISO instant it prints as, 2026-07-12T06:00:00. What is
 * written back is what LocalDateTime prints, which drops a zero second field.
 */
public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime>
{
	@Override
	public void write(JsonWriter writer, LocalDateTime dateTime) throws IOException
	{
		if (null == dateTime)
		{
			writer.nullValue();
			return;
		}
		String text = dateTime.toString();
		writer.value(text);
	}

	@Override
	public LocalDateTime read(JsonReader reader) throws IOException
	{
		if (JsonToken.NULL == reader.peek())
		{
			reader.nextNull();
			return null;
		}
		String text = reader.nextString();
		LocalDateTime dateTime = LocalDateTime.parse(text);
		return dateTime;
	}
}
