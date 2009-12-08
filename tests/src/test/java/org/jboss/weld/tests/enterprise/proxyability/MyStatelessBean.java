package org.jboss.weld.tests.enterprise.proxyability;

import java.io.Serializable;

import javax.ejb.Stateless;

/**
 * Session Bean implementation class MyStatelessBean
 */
@Stateless
public class MyStatelessBean implements MyStatelessBeanLocal, Serializable
{

   public String getText()
   {
      return "This is my message from my stateless bean";
   }
}
