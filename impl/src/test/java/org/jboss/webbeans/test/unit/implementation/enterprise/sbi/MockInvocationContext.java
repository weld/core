package org.jboss.webbeans.test.unit.implementation.enterprise.sbi;

import java.lang.reflect.Method;
import java.util.Map;

import javax.interceptor.InvocationContext;

public class MockInvocationContext implements InvocationContext
{

   private Foo foo = new Foo();
   
   public Map<String, Object> getContextData()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Method getMethod()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Object[] getParameters()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Object getTarget()
   {
      return foo;
   }

   public Object proceed() throws Exception
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void setParameters(Object[] params)
   {
      // TODO Auto-generated method stub
      
   }
   
}
