package org.jboss.webbeans.examples.conversations;

import java.io.Serializable;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.inject.Named;

import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;

@ConversationScoped
@Named
public class Data implements Serializable
{
	private static LogProvider log = Logging.getLogProvider(Data.class);
	private String data;

	public Data()
	{
	}
	
	public String getData()
	{
		return data;
	}
	
	public void setData(String data)
	{
		this.data = data;
	}
	
   public void longop() {
      try 
      {
         Thread.sleep(5000);
      } 
      catch (InterruptedException e) 
      {
         // non-issue
      }
   }
   
   @PreDestroy
   public void bye() 
   {
   		log.info("Data " + data + " destroyed");
   }
   
}