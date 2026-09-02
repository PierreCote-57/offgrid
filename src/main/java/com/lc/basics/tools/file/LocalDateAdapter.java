package com.lc.basics.tools.file;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;

/**
 * A LocalDate on the wire is the ISO day it prints as, 2026-08-17. Gson carries no opinion
 * about java.time, so this is what gives it one.
 */
public class LocalDateAdapter extends TypeAdapter<LocalDate>
{
	@Override
	public void write(JsonWriter writer, LocalDate date) throws IOException
	{
		if (null == date)
		{
			writer.nullValue();
			return;
		}
		String text = date.toString();
		writer.value(text);
	}

	@Override
	public LocalDate read(JsonReader reader) throws IOException
	{
		if (JsonToken.NULL == reader.peek())
		{
			reader.nextNull();
			return null;
		}
		String text = reader.nextString();
		LocalDate date = LocalDate.parse(text);
		return date;
	}
}
