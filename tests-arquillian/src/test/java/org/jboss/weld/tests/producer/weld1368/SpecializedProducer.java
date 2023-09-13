package org.jboss.weld.tests.producer.weld1368;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Specializes;
import jakarta.enterprise.inject.Typed;

/**
 * See http://issues.jboss.org/browse/WELD-1368
 *
 * @author tremes
 *
 */
@Dependent
public class SpecializedProducer {

    @Typed
    @Dependent
    public static class TestBean1 {

    }

    @Dependent
    public static class TestProducer1 {

        @Produces
        public TestBean1 testBean() {
            return new TestBean1();
        }
    }

    @Typed
    @Dependent
    public static class TestBean2 extends TestBean1 {

    }

    @Dependent
    public static class TestProducer2 extends TestProducer1 {

        public void anyMethod() {

        }

        @Produces
        @Specializes
        @Override
        public TestBean2 testBean() {
            return new TestBean2();
        }

    }

    @Typed
    @Dependent
    public static class TestBean3 extends TestBean2 {

    }

    @Dependent
    public static class TestProducer3 extends TestProducer2 {

        @Produces
        @Specializes
        @Override
        public TestBean3 testBean() {
            return new TestBean3();
        }

    }
}
