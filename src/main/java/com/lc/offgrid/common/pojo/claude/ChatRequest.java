package com.lc.offgrid.common.pojo.claude;

import java.util.List;

/**
 * What the chat page posts: the whole transcript, oldest first, the visitor's new line last,
 * and where and when the visitor is asking from. The server keeps nothing between turns.
 */
public class ChatRequest
{
	private List<ChatMessage>	messageList;

	// What the browser states about the visitor. Any of the three may be absent: the zone is
	// the browser's own setting, the coordinates are the visitor's to grant.
	private String				timeZone;
	private Double				latitudeDeg;
	private Double				longitudeDeg;

	public List<ChatMessage> getMessageList()
	{
		return messageList;
	}
	public void setMessageList(List<ChatMessage> messageList)
	{
		this.messageList = messageList;
	}

	public String getTimeZone()
	{
		return timeZone;
	}
	public void setTimeZone(String timeZone)
	{
		this.timeZone = timeZone;
	}

	public Double getLatitudeDeg()
	{
		return latitudeDeg;
	}
	public void setLatitudeDeg(Double latitudeDeg)
	{
		this.latitudeDeg = latitudeDeg;
	}

	public Double getLongitudeDeg()
	{
		return longitudeDeg;
	}
	public void setLongitudeDeg(Double longitudeDeg)
	{
		this.longitudeDeg = longitudeDeg;
	}
}
