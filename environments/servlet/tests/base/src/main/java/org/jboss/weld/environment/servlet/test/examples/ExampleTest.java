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
package org.jboss.weld.environment.servlet.test.examples;

import static org.jboss.weld.environment.servlet.test.util.Deployments.baseDeployment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ExampleTest {

    @Deployment
    public static WebArchive createTestArchive() {
        return baseDeployment().addPackage(ExampleTest.class.getPackage());
    }

    @Test
    public void testGameGenerator(Game game1, Game game2, Generator generator1, Generator generator2) throws Exception {
        assertNotNull(game1);
        assertNotNull(game2);
        assertNotSame(game1, game2);
        assertNotSame(game1.getNumber(), game2.getNumber());

        assertNotNull(generator1);
        assertNotNull(generator2);
        assertNotNull(generator1.getRandom());
        assertEquals(generator1.getRandom(), generator2.getRandom());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSentenceTranslator(TextTranslator textTranslator) {
        textTranslator.translate("foo");
    }

}
