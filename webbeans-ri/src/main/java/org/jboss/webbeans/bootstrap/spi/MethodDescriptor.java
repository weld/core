package org.jboss.webbeans.bootstrap.spi;

public interface MethodDescriptor
{
   
   public String getMethodName();
   
   public Class<?>[] getMethodParameterTypes();
   
}
