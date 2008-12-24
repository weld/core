package org.jboss.webbeans.test.mock;

import java.util.Hashtable;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public class MockInitialContext extends InitialContext
{

   public MockInitialContext(Hashtable<?, ?> arg0) throws NamingException
   {
      super(arg0);
      // TODO Auto-generated constructor stub
   }
   
}
