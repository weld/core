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
package org.jboss.weld.environment.se.test.provider;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.environment.se.test.arquillian.WeldSEClassPath;
import org.junit.runner.RunWith;

/**
 * @author Jozef Hartinger
 *
 * @see WELD-1682
 */
@RunWith(Arquillian.class)
public class MultiModuleCDIProviderTest extends AbstractCDIProviderTest {

    @Deployment
    public static Archive<?> getArchive() {
        JavaArchive bda1 = ShrinkWrap.create(BeanArchive.class).addClasses(Boy.class, Chick.class, Child.class, Female.class,
                Girl.class, KarateClub.class, Male.class, Pretty.class, PrettyLiteral.class);
        JavaArchive bda2 = ShrinkWrap.create(BeanArchive.class).addClasses(Foo.class);
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class).addClass(KarateClubLocator.class);
        return ShrinkWrap.create(WeldSEClassPath.class).add(bda1, bda2, jar);
    }
}
