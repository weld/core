package org.jboss.weld.tests.enterprise.proxyability;

import javax.ejb.Local;

@Local
public interface MyStatelessBeanLocal
{

   String getText();
}
