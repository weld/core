/**
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Matus Abaffy
 */
@RunWith(Arquillian.class)
public class BasicCDIProviderTest extends AbstractCDIProviderTest {

    @Deployment
    public static Archive<?> getArchive() {
        return ShrinkWrap.create(BeanArchive.class).addClasses(Boy.class, Chick.class, Child.class, Female.class, Girl.class,
                KarateClub.class, KarateClubLocator.class, Male.class, Pretty.class, PrettyLiteral.class);
    }

    @Test
    public void testCDIProvider() {
        BeanManager manager = KarateClubLocator.getBeanManager();
        assertNotNull(manager);
        // Boy, Girl, Chick
        assertEquals(3, manager.getBeans(Child.class, Any.Literal.INSTANCE).size());
        // Chick
        assertEquals(1, manager.getBeans(Girl.class, PrettyLiteral.INSTANCE).size());

        KarateClub club = KarateClubLocator.lookupKarateClub();
        assertNotNull(club);
        assertTrue(club.kick());
    }
}
