/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.weld.environment.se.test.beandiscovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import jakarta.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.JarIndexer;
import org.jboss.jandex.Result;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.BeanDiscoveryMode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.jboss.weld.environment.se.test.arquillian.WeldSEClassPath;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class BeanDiscoveryWithJandexIndexTest {
    @Deployment
    public static Archive<?> getDeployment() {
        WeldSEClassPath archives = ShrinkWrap.create(WeldSEClassPath.class);
        JavaArchive archive01 = ShrinkWrap.create(BeanArchive.class).addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsManifestResource(EmptyAsset.INSTANCE, "jandex.idx") // simulate broken index
                .addClasses(Dog.class, Cat.class, Cow.class);
        archives.add(archive01);

        JavaArchive archive02 = ShrinkWrap.create(BeanArchive.class)
                .addAsManifestResource(new BeansXml(BeanDiscoveryMode.ANNOTATED), "beans.xml")
                .addClasses(Plant.class, Tree.class, Stone.class);
        archive02.addAsManifestResource(createJandexIndexAsset(archive02), "jandex.idx");
        archives.add(archive02);

        JavaArchive archive03 = ShrinkWrap.create(BeanArchive.class)
                .addAsManifestResource(new BeansXml(BeanDiscoveryMode.ALL), "beans.xml")
                .addClasses(Flat.class, House.class, Apartment.class);
        archive03.addAsManifestResource(createJandexIndexAsset(archive03), "jandex.idx");
        archives.add(archive03);

        // Archive without index
        JavaArchive archive04 = ShrinkWrap.create(BeanArchive.class)
                .addAsManifestResource(new BeansXml(BeanDiscoveryMode.ALL), "beans.xml")
                .addClasses(Hat.class);
        archives.add(archive04);

        return archives;
    }

    /**
     * Exports the JavaArchive to a temporary file and uses {@link JarIndexer} to write an jandex.idx file.
     */
    private static Asset createJandexIndexAsset(JavaArchive archiveToIndex) {
        Asset jandexIndexAsset = EmptyAsset.INSTANCE;
        try {
            final File tempJarFile = File.createTempFile("BeanDiscoveryWithJandexIndexTest", ".jar");
            tempJarFile.deleteOnExit();

            archiveToIndex.as(ZipExporter.class).exportTo(tempJarFile, true);

            final Indexer indexer = new Indexer();
            final Result result = JarIndexer.createJarIndex(tempJarFile, indexer, false, false, false);

            final File indexFile = result.getOutputFile();
            indexFile.deleteOnExit();

            jandexIndexAsset = new FileAsset(indexFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return jandexIndexAsset;
    }

    @Test
    public void testIndexedAllBeanDiscoveryForBrokenIndex(BeanManager manager) {
        assertEquals(1, manager.getBeans(Dog.class).size());
        assertEquals(1, manager.getBeans(Cat.class).size());
        assertEquals(1, manager.getBeans(Cow.class).size());
    }

    @Test
    public void testIndexedAnnotatedBeanDiscovery(BeanManager manager) {
        assertEquals(1, manager.getBeans(Plant.class).size());
        assertEquals(1, manager.getBeans(Tree.class).size());
        assertEquals(0, manager.getBeans(Stone.class).size()); // not annotated!
    }

    @Test
    public void testIndexedAllBeanDiscovery(BeanManager manager) {
        assertEquals(1, manager.getBeans(Flat.class).size());
        assertEquals(1, manager.getBeans(House.class).size());
        assertEquals(1, manager.getBeans(Apartment.class).size());
    }

    @Test
    public void testArchiveWithoutIndex(Hat hat) {
        assertNotNull(hat);
        assertNotNull(hat.getBeanManager());
    }

}
