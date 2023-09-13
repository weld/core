/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.tests.unit.bootstrap.xml;

import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.container.weld.embedded.mock.TestContainer;
import org.jboss.arquillian.container.weld.embedded.mock.TestContainer.Runner;
import org.jboss.arquillian.container.weld.embedded.mock.TestContainer.Runner.Runnable;
import org.jboss.weld.bootstrap.enablement.ModuleEnablement;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.logging.XmlLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.api.WeldManager;
import org.testng.Assert;
import org.testng.annotations.Test;

@SuppressWarnings("unchecked")
public class BeansXmlTest {

    private static Runner createRunner(String... beansXmls) {
        List<Class<?>> beanClasses = Arrays.asList(Alt.class, Dec.class, Int.class, Plain.class, IntBind.class);
        List<URL> beansXmlsList = new ArrayList<URL>();
        for (String beansXml : beansXmls) {
            beansXmlsList.add(BeansXmlTest.class.getResource(beansXml));
        }
        return new TestContainer.Runner(beansXmlsList, beanClasses);
    }

    // Multiple XML blocks

    @Test
    public void testMultipleAlternativeBlocksFail() {
        createRunner("multipleAlternativeBlocks.xml").runAndExpect(XmlLogger.LOG.multipleAlternatives(null));
    }

    @Test
    public void testMultipleDecoratorBlocksFail() {
        createRunner("multipleDecoratorBlocks.xml").runAndExpect(XmlLogger.LOG.multipleDecorators(null));
    }

    @Test
    public void testMultipleInterceptorBlocksFail() {
        createRunner("multipleInterceptorsBlocks.xml").runAndExpect(XmlLogger.LOG.multipleInterceptors(null));
    }

    @Test
    public void testAlternativesEnabled() throws Exception {
        createRunner("alternative.xml").run(new Runnable() {

            public void run(WeldManager beanManager) {
                ModuleEnablement enabled = ((BeanManagerImpl) beanManager).getEnabled();
                assertEquals(1, enabled.getAlternativeClasses().size());
                assertEquals(Alt.class, enabled.getAlternativeClasses().iterator().next());
            }

        });
    }

    @Test
    public void testDecoratorsEnabled() throws Exception {
        createRunner("decorator.xml").run(new Runnable() {

            public void run(WeldManager beanManager) {
                ModuleEnablement enabled = ((BeanManagerImpl) beanManager).getEnabled();
                assertEquals(1, enabled.getDecorators().size());
                assertEquals(Dec.class, enabled.getDecorators().iterator().next());

            }
        });
    }

    @Test
    public void testInterceptorsEnabled() throws Exception {
        createRunner("interceptor.xml").run(new Runnable() {

            public void run(WeldManager beanManager) {
                ModuleEnablement enabled = ((BeanManagerImpl) beanManager).getEnabled();
                assertEquals(3, enabled.getInterceptors().size());
                assertTrue(enabled.getInterceptors().contains(Int.class));
            }
        });
    }

    @Test
    public void testMergeBeansXmls() throws Exception {
        createRunner("alternative.xml", "decorator.xml", "interceptor.xml").run(new Runnable() {

            public void run(WeldManager beanManager) {
                ModuleEnablement enabled = ((BeanManagerImpl) beanManager).getEnabled();
                Assert.assertEquals(enabled.getAlternativeClasses().size(), 1);
                Assert.assertEquals(Alt.class, enabled.getAlternativeClasses().iterator().next());
                Assert.assertEquals(enabled.getInterceptors().size(), 3);
                Assert.assertTrue(enabled.getInterceptors().contains(Int.class));
                Assert.assertEquals(enabled.getDecorators().size(), 1);
                Assert.assertEquals(Dec.class, enabled.getDecorators().iterator().next());
            }
        });
    }

    @Test
    public void testBeansXmlDoesntExist() {
        createRunner("nope.xml").runAndExpect(XmlLogger.LOG.loadError(null, null));
    }

    // WELD-467
    @Test
    public void testNamespacedBeansXml() throws Exception {
        createRunner("namespaced.xml").run(new Runnable() {

            public void run(WeldManager beanManager) {
                ModuleEnablement enabled = ((BeanManagerImpl) beanManager).getEnabled();
                assertEquals(1, enabled.getAlternativeClasses().size());
                assertEquals(Alt.class, enabled.getAlternativeClasses().iterator().next());
            }
        });
    }

    // WELD-467
    @Test
    public void testNotDefaultNamespacedBeansXml() throws Exception {
        createRunner("nonDefaultNamespaced.xml").run(new Runnable() {

            public void run(WeldManager beanManager) {
                ModuleEnablement enabled = ((BeanManagerImpl) beanManager).getEnabled();
                assertEquals(1, enabled.getAlternativeClasses().size());
                assertEquals(Alt.class, enabled.getAlternativeClasses().iterator().next());
            }
        });
    }

    /*
     * https://jira.jboss.org/jira/browse/WELD-362
     */
    @Test
    public void testNonPrettyPrintedXML() throws Exception {
        createRunner("nonPrettyPrinted.xml").run(new Runnable() {

            public void run(WeldManager beanManager) {
                ModuleEnablement enabled = ((BeanManagerImpl) beanManager).getEnabled();
                assertEquals(1, enabled.getAlternativeClasses().size());
                assertEquals(Alt.class, enabled.getAlternativeClasses().iterator().next());
            }
        });
    }

    @Test
    public void testCannotLoadFile() throws MalformedURLException {
        createRunner("http://foo.bar/beans.xml").runAndExpect(XmlLogger.LOG.loadError(null, null));
    }

    @Test
    public void testParsingError() {
        createRunner("unparseable.xml").runAndExpect(XmlLogger.LOG.parsingError(null, null));
    }

    @Test
    public void testCannotLoadClass() {
        createRunner("unloadable.xml").runAndExpect(new DeploymentException(new Exception()));
    }

}
