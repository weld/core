package org.jboss.weld.environment.servlet.test.context.async;

import static org.jboss.weld.environment.servlet.test.util.JettyDeployments.JETTY_ENV;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 * @author Tomas Remes
 */

@RunWith(Arquillian.class)
public class SimpleAsyncListenerTest extends SimpleAsyncListenerTestBase {

    @Deployment(testable = false)
    public static WebArchive deployment() {
        return SimpleAsyncListenerTestBase.deployment().addAsWebInfResource(JETTY_ENV, "jetty-env.xml");
    }

}
