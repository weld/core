package org.jboss.weld.environment.se.test.beandiscovery;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import javax.enterprise.inject.spi.BeanManager;

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
        JavaArchive archive01 = ShrinkWrap.create(BeanArchive.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
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
        return archives;
    }

    /**
     * Exports the JavaArchive to a temporary file and uses {@link JarIndexer} to write an 
     * jandex.idx file.  
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
}
