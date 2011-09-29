package org.jboss.weld.tests.resources.producer.non.dependent;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(Integration.class)
public class NonDependentResourceProducerFieldTest {

    @Deployment
    @ShouldThrowException(DefinitionException.class)
    public static JavaArchive deploy() {
        return ShrinkWrap.create(BeanArchive.class)
                .addPackage(NonDependentResourceProducerFieldTest.class.getPackage());

    }


    @Test
    public void test() {
    }

}
