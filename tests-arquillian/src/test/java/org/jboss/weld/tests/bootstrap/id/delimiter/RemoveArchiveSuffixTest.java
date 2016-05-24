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

import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
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

/** @see WELD-2064
 * Tests the configuration option -> ROLLING_UPGRADES_ID_DELIMITER
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@RunWith(Arquillian.class)
@Category(Integration.class)
public class RemoveArchiveSuffixTest {

    private static final String AFFIX = "version-1-1.war";   
    private static final String ARCHIVE_NAME = "archive";
    private static final String DELIMITER = "__";
    private static final String FULL_ARCHIVE_NAME = ARCHIVE_NAME + DELIMITER + AFFIX;


    @Inject
    private AllKnowingBean allKnowingBean;
    @Inject
    private BeanManager manager;
    
    @Deployment
    public static WebArchive getDeployment() {
        WebArchive testDeployment = ShrinkWrap
            .create(WebArchive.class, FULL_ARCHIVE_NAME)
            .addPackage(RemoveArchiveSuffixTest.class.getPackage())
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addAsResource(PropertiesBuilder.newBuilder().set(ConfigurationKey.ROLLING_UPGRADES_ID_DELIMITER.get(), DELIMITER).build(),
                "weld.properties");
        return testDeployment;
    }

    @Test
    public void testIdWasChangedCorrectly() {
        allKnowingBean.getAnswerToLifeUniverseAndEverything();
        Set<Bean<?>> beans = manager.getBeans(AllKnowingBean.class);
        
        // there should only be one bean
        assertTrue(beans.size() == 1);
        
        // cast to ManagedBean to be abble to obtain the identifier
        String identifier = ((ManagedBean<AllKnowingBean>) beans.iterator().next()).getIdentifier().asString();
        
        // resulting ID should have archiveName, but should NOT have delimiter and affix
        assertTrue(identifier.contains(ARCHIVE_NAME));
        assertFalse(identifier.contains(DELIMITER));
        assertFalse(identifier.contains(AFFIX));
        
    }
}
