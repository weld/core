package org.jboss.weld.environment.servlet.test.injection;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

import static org.jboss.weld.environment.servlet.test.util.TomcatDeployments.CONTEXT_XML;


@RunWith(Arquillian.class)
public class ListenerInjectionTest extends ListenerInjectionTestBase {

    @Deployment(testable = false)
    public static WebArchive deployment() {
        return ListenerInjectionTestBase.deployment().add(CONTEXT_XML, "META-INF/context.xml");
    }

}
