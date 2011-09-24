package org.jboss.weld.environment.servlet.test.examples;

import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.environment.servlet.test.util.BeansXml;
import org.junit.Test;

import static org.jboss.weld.environment.servlet.test.util.Deployments.baseDeployment;
import static org.junit.Assert.assertEquals;

public class MockExampleTestBase {

    public static WebArchive deployment() {
        return baseDeployment(new BeansXml().alternatives(MockSentenceTranslator.class)).addPackage(MockExampleTestBase.class.getPackage());
    }

    @Test
    public void testMockSentenceTranslator(TextTranslator textTranslator) throws Exception {
        assertEquals("Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet.", textTranslator.translate("Hello world. How's tricks?"));
    }

}
