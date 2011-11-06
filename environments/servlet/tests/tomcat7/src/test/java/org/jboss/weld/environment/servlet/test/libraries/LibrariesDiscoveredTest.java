package org.jboss.weld.environment.servlet.test.libraries;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

import java.util.logging.Logger;

import static org.jboss.weld.environment.servlet.test.util.TomcatDeployments.CONTEXT_XML;

@RunWith(Arquillian.class)
public class LibrariesDiscoveredTest extends LibrariesDiscoveredTestBase {

    private static final Logger log = Logger.getLogger(LibrariesDiscoveredTest.class.getName());

    @Deployment
    public static WebArchive deployment() {
        WebArchive archive = LibrariesDiscoveredTestBase.deployment().add(CONTEXT_XML, "META-INF/context.xml");
        log.fine("-------------------------");
        log.fine("War");
        log.fine("-------------------------");
        log.fine(archive.toString(true));
        return archive;
    }

}
