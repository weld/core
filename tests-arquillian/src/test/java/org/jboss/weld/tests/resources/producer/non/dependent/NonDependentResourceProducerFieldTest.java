package org.jboss.weld.tests.resources.producer.non.dependent;

import jakarta.enterprise.inject.spi.DefinitionException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(Integration.class)
public class NonDependentResourceProducerFieldTest {

    public static final Asset PERSISTENCE_XML = new ByteArrayAsset(
            "<persistence xmlns=\"http://java.sun.com/xml/ns/persistence\" version=\"1.0\"><persistence-unit name=\"pu1\"><jta-data-source>java:jboss/datasources/ExampleDS</jta-data-source></persistence-unit></persistence>"
                    .getBytes());

    @Deployment
    @ShouldThrowException(DefinitionException.class)
    public static JavaArchive deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(NonDependentResourceProducerFieldTest.class))
                .addPackage(NonDependentResourceProducerFieldTest.class.getPackage())
                .addAsResource(PERSISTENCE_XML, "META-INF/persistence.xml");
    }

    @Test
    public void testDeploymentWithNonDependentResourceProducerField() {
        // should throw definition exception
    }

}
