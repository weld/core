package org.jboss.webbeans.examples.conversations;

import java.io.Serializable;

import javax.annotation.Named;
import javax.context.ConversationScoped;

@ConversationScoped
@Named
public class Data implements Serializable
{
	private String data;

	public String getData()
	{
		return data;
	}
	
	public void setData(String data)
	{
		this.data = data;
	}
}