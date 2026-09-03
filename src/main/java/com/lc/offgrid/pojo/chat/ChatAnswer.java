package com.lc.offgrid.pojo.chat;

import com.lc.offgrid.spring.tools.RestBaseAnswer;

/**
 * The chat's reply. One line of text; the timing comes from RestBaseAnswer.
 */
public class ChatAnswer extends RestBaseAnswer
{
	private String		text;

	public ChatAnswer(String text)
	{
		this.text = text;
	}

	public String getText()
	{
		return text;
	}
}
