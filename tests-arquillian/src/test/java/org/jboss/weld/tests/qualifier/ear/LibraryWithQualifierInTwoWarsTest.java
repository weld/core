/*
 * JBoss, Home of Professional Open Source
 * Copyright 2018, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.qualifier.ear;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @see https://issues.jboss.org/browse/WELD-2250
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@RunWith(Arquillian.class)
@RunAsClient
@Category(Integration.class)
public class LibraryWithQualifierInTwoWarsTest {

    @Deployment
    public static Archive<?> getDeployment() {
        // setup JAR with annotation
        JavaArchive annotationArchive = ShrinkWrap.create(JavaArchive.class).addClass(SomeQualifier.class);

        // setup WARs
        WebArchive war1 = ShrinkWrap.create(WebArchive.class, "test1.war")
                .addClasses(FooBean.class)
                .addAsLibraries(annotationArchive);

        WebArchive war2 = ShrinkWrap.create(WebArchive.class, "test2.war")
                .addClasses(BarBean.class)
                .addAsLibraries(annotationArchive);

        // setup EAR
        EnterpriseArchive ear = ShrinkWrap
                .create(EnterpriseArchive.class,
                        Utils.getDeploymentNameAsHash(LibraryWithQualifierInTwoWarsTest.class, Utils.ARCHIVE_TYPE.EAR))
                .addAsModules(war1, war2);
        return ear;
    }

    @Test
    public void test() {
        // verification is merely the ability to deploy this scenario
    }
}
