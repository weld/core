package org.jboss.weld.environment.servlet.test.crosscontext;



import static org.jboss.weld.environment.servlet.test.util.JettyDeployments.JETTY_ENV;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;




@RunWith(Arquillian.class)
public class CrossContextForwardTest extends CrossContextForwardTestBase {


    @Deployment(name = CrossContextForwardTestBase.FIRST, testable = false)
    public static WebArchive createFirstTestArchive() {
        return CrossContextForwardTestBase.createFirstTestArchive().addAsWebInfResource(JETTY_ENV, "jetty-env.xml");
    }

    @Deployment(name = CrossContextForwardTestBase.SECOND, testable = false)
    public static WebArchive createSecondTestArchive() {
        return CrossContextForwardTestBase.createSecondTestArchive().addAsWebInfResource(JETTY_ENV, "jetty-env.xml");
    }

}