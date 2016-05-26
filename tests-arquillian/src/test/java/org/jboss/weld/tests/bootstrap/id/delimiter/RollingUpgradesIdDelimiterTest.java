/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.bootstrap.id.delimiter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.bean.ManagedBean;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.tests.category.Integration;
import org.jboss.weld.tests.util.PropertiesBuilder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @see WELD-2064 Tests the configuration option -> ROLLING_UPGRADES_ID_DELIMITER
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
@Category(Integration.class)
public class RollingUpgradesIdDelimiterTest {

    private static final String WAR_SUFFIX = ".war";
    private static final String JAR_SUFFIX = ".jar";
    private static final String VERSION = "version-1-1";
    private static final String ARCHIVE_NAME = "archive";
    private static final String DELIMITER = "__";
    private static final String FULL_ARCHIVE_NAME = ARCHIVE_NAME + DELIMITER + VERSION + WAR_SUFFIX;

    @Deployment
    public static WebArchive createTestArchive() {
        WebArchive testDeployment = ShrinkWrap.create(WebArchive.class, FULL_ARCHIVE_NAME)
                .addClasses(RollingUpgradesIdDelimiterTest.class, AllKnowingBean.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsLibraries(ShrinkWrap.create(BeanArchive.class, ARCHIVE_NAME + DELIMITER + VERSION + JAR_SUFFIX)
                        .addClass(LittleBean.class))
                .addAsResource(PropertiesBuilder.newBuilder()
                        .set(ConfigurationKey.ROLLING_UPGRADES_ID_DELIMITER.get(), DELIMITER).build(), "weld.properties");
        return testDeployment;
    }

    @Test
    public void testIdWasChangedCorrectly(AllKnowingBean allKnowingBean, LittleBean littleBean, BeanManager manager) {
        allKnowingBean.getAnswerToLifeUniverseAndEverything();
        littleBean.ping();

        ManagedBean<?> bean = (ManagedBean<?>) manager.resolve(manager.getBeans(AllKnowingBean.class));
        assertIdentifier(bean.getIdentifier().asString(), WAR_SUFFIX);

        bean = (ManagedBean<?>) manager.resolve(manager.getBeans(LittleBean.class));
        assertIdentifier(bean.getIdentifier().asString(), JAR_SUFFIX);
    }

    private void assertIdentifier(String identifier, String suffix) {
        // resulting ID should have archiveName and suffix, but should NOT have delimiter and version
        assertTrue("Missing archive name: " + identifier, identifier.contains(ARCHIVE_NAME));
        assertTrue("Missing suffix: " + identifier, identifier.contains(suffix));
        assertFalse("Contains delimiter: " + identifier, identifier.contains(DELIMITER));
        assertFalse("Contains version: " + identifier, identifier.contains(VERSION));
    }

}
