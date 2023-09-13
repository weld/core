/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.servlet.test.bootstrap.enhanced;

import static org.jboss.weld.environment.servlet.test.util.Deployments.baseDeployment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.environment.servlet.test.util.Deployments;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verify the <code>org.jboss.weld.environment.servlet.EnhancedListener</code> works correctly on its own, i.e. if the
 * <code>org.jboss.weld.environment.servlet.Listener</code> is not configured in web.xml.
 *
 * Note that the test suite is using both Listener and EnhancedListener for all the tests. For Tomcat prior to 7.0.52 a
 * workaround was required in embedded mode
 * due to Maven Surefire classloading issues. See also
 * <a href="http://maven.apache.org/surefire/maven-surefire-plugin/examples/class-loading.html">Classloading
 * and Forking in Maven Surefire</a>.
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class EnhancedListenerTest {

    public static final Asset WEB_XML = new ByteArrayAsset(
            (Deployments.DEFAULT_WEB_XML_START + Deployments.DEFAULT_WEB_XML_SUFFIX).getBytes());

    @Deployment
    public static WebArchive createTestArchive() {
        return baseDeployment(WEB_XML).addPackage(EnhancedListenerTest.class.getPackage());
    }

    @Test
    public void testAloneEnhancedListener(Ping ping) {
        // Test whether bootstrap finished successfully
        assertNotNull(ping);
        assertEquals(Integer.valueOf(1), ping.getObservations());
        assertEquals(Integer.valueOf(1), ping.getObjectObservations());
    }

}
