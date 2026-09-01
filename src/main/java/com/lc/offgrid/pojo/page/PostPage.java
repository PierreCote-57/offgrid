package com.lc.offgrid.pojo.page;

import java.time.LocalDateTime;

/**
 * A blog entry. It is the only kind of page that happened on a day.
 */
public class PostPage extends PageData
{
	private LocalDateTime	date;

	public LocalDateTime getDate()
	{
		return date;
	}
}
