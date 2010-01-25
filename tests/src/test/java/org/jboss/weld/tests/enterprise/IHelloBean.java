package org.jboss.weld.tests.enterprise;

import javax.ejb.Local;

@Local
public interface IHelloBean
{

   public abstract String sayHello();

   public abstract String sayGoodbye();

   public abstract void remove();

}