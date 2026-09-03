package com.lc.offgrid.pojo.chat;

import java.util.List;

/**
 * What the chat page posts: the whole transcript, oldest first, the visitor's new line last.
 * The server keeps nothing between turns.
 */
public class ChatRequest
{
	private List<ChatMessage>	messageList;

	public List<ChatMessage> getMessageList()
	{
		return messageList;
	}
	public void setMessageList(List<ChatMessage> messageList)
	{
		this.messageList = messageList;
	}
}
