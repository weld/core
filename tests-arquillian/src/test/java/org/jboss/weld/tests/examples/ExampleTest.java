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
package org.jboss.weld.tests.examples;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ExampleTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ExampleTest.class))
                .addPackage(ExampleTest.class.getPackage());
    }

    @Test
    public void testGameGenerator(Game game1, Game game2, Generator gen1, Generator gen2) throws Exception {
        Assert.assertNotSame(game1, game2);
        Assert.assertNotSame(game1.getNumber(), game2.getNumber());

        Assert.assertNotNull(gen1.getRandom());
        Assert.assertEquals(gen1.getRandom(), gen2.getRandom());
    }

    @Test
    public void testSentenceTranslator(TextTranslator tt1) throws Exception {
        try {
            tt1.translate("hello world");
            Assert.fail();
        } catch (UnsupportedOperationException uoe) {
            //expected
        }
    }

}
