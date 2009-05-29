package org.jboss.webbeans.examples.conversations;

import java.io.Serializable;

import javax.enterprise.context.ConversationScoped;
import javax.enterprise.inject.Named;

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