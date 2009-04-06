package org.jboss.webbeans.resources;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import org.jboss.webbeans.CurrentManager;

public class ManagerObjectFactory implements ObjectFactory
{
   
   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception
   {
      return CurrentManager.rootManager().getCurrent();
   }
   
}
