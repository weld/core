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
package org.jboss.weld.environment.servlet.test.bootstrap.nobeanarchive;

import jakarta.enterprise.inject.spi.DefinitionException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.environment.servlet.test.util.Deployments;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verify the <code>org.jboss.weld.environment.servlet.EnhancedListener</code> does not fail if the deployment does not contain any bean archive.
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class NoBeanArchiveTest {

    public static final Asset WEB_XML = new ByteArrayAsset((Deployments.DEFAULT_WEB_XML_START + Deployments.DEFAULT_WEB_XML_SUFFIX).getBytes());

    @ShouldThrowException(IllegalStateException.class)
    @Deployment()
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class).setWebXML(WEB_XML);
    }

    @Test
    @Ignore
    public void testDeploymentDoesNotFail() {
    }

}
