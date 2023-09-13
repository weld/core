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
package org.jboss.weld.tests.resources;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@Category(Integration.class)
@RunWith(Arquillian.class)
public class ResourceTest {
    @Deployment // changed to .war, from .jar
    public static Archive<?> deploy() {
        return ShrinkWrap.create(WebArchive.class, Utils.getDeploymentNameAsHash(ResourceTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addClasses(ResourceTest.class, UTConsumer.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    /*
     * description = "WELD-385"
     */
    @Test
    public void testUTInjectedByResource(UTConsumer consumer) {
        Assert.assertNotNull(consumer.getUserTransaction());
    }
}
