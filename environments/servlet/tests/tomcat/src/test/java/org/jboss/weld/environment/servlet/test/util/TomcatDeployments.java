package org.jboss.weld.environment.servlet.test.util;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;

public class TomcatDeployments
{

   public static final Asset CONTEXT_XML = new ByteArrayAsset("<Context> <Manager pathname=\"\" /> <Resource name=\"BeanManager\" auth=\"Container\" type=\"javax.inject.manager.BeanManager\" factory=\"org.jboss.weld.resources.ManagerObjectFactory\"/> <Listener className=\"org.jboss.weld.environment.tomcat.WeldLifecycleListener\"/></Context>".getBytes());

}
