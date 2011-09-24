package org.jboss.weld.environment.servlet.test.util;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;

public class JettyDeployments {

    public static final Asset JETTY_ENV = new StringAsset("<Configure id=\"webAppCtx\" class=\"org.mortbay.jetty.webapp.WebAppContext\"><New class=\"org.mortbay.jetty.plus.naming.EnvEntry\"><Arg><Ref id=\"webAppCtx\"/></Arg><Arg>BeanManager</Arg><Arg><New class=\"javax.naming.Reference\"><Arg>javax.enterprise.inject.spi.BeanManager</Arg><Arg>org.jboss.weld.resources.ManagerObjectFactory</Arg><Arg/></New></Arg><Arg type=\"boolean\">true</Arg></New></Configure>");

    public static final Asset JETTY_WEB = new StringAsset("<Configure id=\"webAppCtx\" class=\"org.mortbay.jetty.webapp.WebAppContext\"><Call class=\"org.jboss.weld.environment.jetty.WeldServletHandler\" name=\"process\"><Arg><Ref id=\"webAppCtx\"/></Arg></Call></Configure>");

}
