package com.lc.offgrid.pojo.chat;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * One line of a chat transcript.
 */
public class ChatMessage
{
	/**
	 * Who said the line. Each constant carries its wire spelling.
	 */
	public enum Role
	{
		@JsonProperty("user")		USER,
		@JsonProperty("assistant")	ASSISTANT
	}

	private Role	role;
	private String	text;

	public Role getRole()
	{
		return role;
	}
	public void setRole(Role role)
	{
		this.role = role;
	}

	public String getText()
	{
		return text;
	}
	public void setText(String text)
	{
		this.text = text;
	}
}
