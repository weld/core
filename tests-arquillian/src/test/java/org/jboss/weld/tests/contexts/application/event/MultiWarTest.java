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
package org.jboss.weld.tests.contexts.application.event;

import javax.servlet.ServletContext;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verifies that an observer is not notified of a non-visible {@link ServletContext}.
 * 
 * @author Jozef Hartinger
 * 
 */
@RunWith(Arquillian.class)
public class MultiWarTest {

    @Deployment(testable = false)
    public static EnterpriseArchive getDeployment() {
        WebArchive war1 = ShrinkWrap.create(WebArchive.class, "test1.war").addClasses(Observer2.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        WebArchive war2 = ShrinkWrap.create(WebArchive.class, "test2.war").addClasses(Observer3.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        return ShrinkWrap.create(EnterpriseArchive.class).addAsModules(war1, war2).addAsManifestResource(MultiWarTest.class.getPackage(), "application.xml", "application.xml");
    }

    @Test
    public void test() {
        // noop - the deployment either fails or not
    }
}
