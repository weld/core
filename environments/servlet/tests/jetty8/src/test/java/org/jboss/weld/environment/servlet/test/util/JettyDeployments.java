package org.jboss.weld.environment.servlet.test.util;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;

public class JettyDeployments
{
   
   public static final Asset JETTY_ENV = new ByteArrayAsset("<Configure id=\"webAppCtx\" class=\"org.eclipse.jetty.webapp.WebAppContext\"><New id=\"BeanManager\" class=\"org.eclipse.jetty.plus.jndi.Resource\"><Arg><Ref id=\"webAppCtx\"/></Arg> <Arg>BeanManager</Arg><Arg><New class=\"javax.naming.Reference\"><Arg>javax.enterprise.inject.spi.BeanManager</Arg><Arg>org.jboss.weld.resources.ManagerObjectFactory</Arg><Arg/></New></Arg></New></Configure>".getBytes());
   
   public static final Asset JETTY_WEB = new ByteArrayAsset("<Configure id=\"webAppCtx\" class=\"org.eclipse.jetty.webapp.WebAppContext\"><Call class=\"org.jboss.weld.environment.jetty8.WeldWebAppDecorator\" name=\"register\"><Arg><Ref id=\"webAppCtx\"/></Arg></Call></Configure>".getBytes());

}
