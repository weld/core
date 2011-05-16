package org.jboss.weld.tests.contexts.request.rmi;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.naming.InitialContext;
import javax.naming.NamingException;

@Category(Integration.class)
@RunWith(Arquillian.class)
@Run(RunModeType.AS_CLIENT)
public class RmiRequestScopeTest {

    @Deployment
    public static WebArchive createDeployment()
    {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addClasses(Bridge.class, BridgeBean.class, Config.class, Manager.class, My.class)
                .addWebResource(RmiRequestScopeTest.class.getPackage(), "web.xml", "web.xml")
                .addWebResource(RmiRequestScopeTest.class.getPackage(), "faces-config.xml", "faces-config.xml")
//                .addWebResource(RmiRequestScopeTest.class.getPackage(), "beans.xml", "beans.xml")
                .addWebResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    private static Bridge getBridge()
    {
        try
        {
            Object serverObject = new InitialContext().lookup("BridgeBean/remote");
            return (Bridge) serverObject;
        }
        catch (NamingException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testRmiRequestScopeActive()
    {
        System.out.println(getBridge().doSomething());
    }
}
