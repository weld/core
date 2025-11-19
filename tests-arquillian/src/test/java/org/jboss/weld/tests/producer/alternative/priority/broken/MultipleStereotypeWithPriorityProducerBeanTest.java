package org.jboss.weld.tests.producer.alternative.priority.broken;

import jakarta.enterprise.inject.spi.DefinitionException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class MultipleStereotypeWithPriorityProducerBeanTest {

    @Deployment
    @ShouldThrowException(DefinitionException.class)
    public static Archive<?> getDeployment() {
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(MultipleStereotypeWithPriorityProducerBeanTest.class))
                .addClasses(BrokenBeanProducer.class, MyStereotype.class, MyOtherStereotype.class);
    }

    @Test
    public void multipleStereotypesFound() {
        // should throw definition exception
    }

}
