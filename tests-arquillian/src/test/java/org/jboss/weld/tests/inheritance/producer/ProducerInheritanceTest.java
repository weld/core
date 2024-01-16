package org.jboss.weld.tests.inheritance.producer;

import java.util.function.Consumer;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @see https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0#member_level_inheritance
 */
public class ProducerInheritanceTest {

    interface Foo {
    }

    interface Parent {
    }

    interface Child {
    }

    @Dependent
    static class FieldParent implements Parent {
        @Produces
        Foo foo = new Foo() {
        };
    }

    @Dependent
    static class FieldChild extends FieldParent implements Child {
    }

    @Dependent
    static class MethodParent implements Parent {
        @Produces
        Foo foo() {
            return new Foo() {
            };
        }
    }

    @Dependent
    static class MethodChild extends MethodParent implements Child {
    }

    interface DefaultMethodParent extends Parent {
        @Produces
        default Foo foo() {
            return new Foo() {
            };
        }
    }

    @Dependent
    static class DefaultMethodChild implements DefaultMethodParent, Child {
    }

    public static class ProcessAnnotatedTypeExtension implements Extension {
        <T extends Parent> void processAnnotatedType(@Observes ProcessAnnotatedType<T> processAnnotatedType) {
            // calling configureAnnotatedType() appears to be sufficient for the AnnotatedType pat.getAnnotatedType()
            // to get replaced after this method processAnnotatedType has returned with (zero or more) changes applied.
            processAnnotatedType.configureAnnotatedType();
        }
    }

    static final Consumer<Foo> assertNotNull = Assert::assertNotNull;
    static final Consumer<Foo> assertNull = Assert::assertNull;

    static class TestCaseBuilder implements Cloneable {
        Class<?> testClass;
        Class<? extends Parent> parentClass;
        Class<? extends Child> childClass;
        boolean deployProcessAnnotatedTypeObserver;
        Consumer<Foo> assertion = assertNotNull;

        protected TestCaseBuilder clone() {
            try {
                return (TestCaseBuilder) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        TestCaseBuilder noParent() {
            TestCaseBuilder builder = clone();
            builder.parentClass = null;
            return builder;
        }

        TestCaseBuilder parent(Class<? extends Parent> parentClass) {
            TestCaseBuilder builder = clone();
            builder.parentClass = parentClass;
            return builder;
        }

        TestCaseBuilder child(Class<? extends Child> childClass) {
            TestCaseBuilder builder = clone();
            builder.childClass = childClass;
            return builder;
        }

        TestCaseBuilder pat() {
            TestCaseBuilder builder = clone();
            builder.deployProcessAnnotatedTypeObserver = true;
            return builder;
        }

        TestCaseBuilder resolvesFoo(boolean resolves) {
            TestCaseBuilder builder = clone();
            builder.assertion = resolves ? assertNotNull : assertNull;
            return builder;
        }

        JavaArchive deploy(Class<?> testClass) {
            this.testClass = testClass;
            JavaArchive archive = ShrinkWrap
                    .create(BeanArchive.class, Utils.getDeploymentNameAsHash(testClass))
                    .addClass(testClass);
            if (parentClass != null) {
                archive = archive.addClass(parentClass);
            }
            archive = archive.addClass(childClass);
            if (deployProcessAnnotatedTypeObserver) {
                archive = archive.addClass(ProcessAnnotatedTypeExtension.class);
            }
            return archive;
        }
    }

    @RunWith(Arquillian.class)
    abstract static class AbstractTest {
        final TestCaseBuilder testCaseBuilder;

        AbstractTest(TestCaseBuilder testCaseBuilder) {
            this.testCaseBuilder = testCaseBuilder;
        }

        @Before
        public void assertSomeAssumptionAboutTheTestItself() {
            Assert.assertEquals(getClass(), testCaseBuilder.testClass);
        }

        @Test
        public void testProducer(Foo foo) {
            testCaseBuilder.assertion.accept(foo);
        }
    }

    static TestCaseBuilder producerField = new TestCaseBuilder().parent(FieldParent.class).child(FieldChild.class);
    static TestCaseBuilder producerMethod = new TestCaseBuilder().parent(MethodParent.class).child(MethodChild.class);

    /**
     * {@code foo} cannot be resolved because neither {@link Parent} nor {@link Child} produce {@link Foo}.
     * Both {@link Parent} and {@link Child}'s producer methods are considered inherited and therefore both
     * don't actually produce.
     */
    static TestCaseBuilder producerDefaultMethod = new TestCaseBuilder().parent(DefaultMethodParent.class)
            .child(DefaultMethodChild.class).resolvesFoo(false);
    static TestCaseBuilder producerFieldNoParent = producerField.noParent().resolvesFoo(false);
    static TestCaseBuilder producerMethodNoParent = producerMethod.noParent().resolvesFoo(false);
    static TestCaseBuilder producerDefaultMethodNoParent = producerDefaultMethod.noParent().resolvesFoo(false);
    static TestCaseBuilder producerFieldPat = producerField.pat();
    static TestCaseBuilder producerMethodPat = producerMethod.pat();
    static TestCaseBuilder producerDefaultMethodPat = producerDefaultMethod.pat();
    static TestCaseBuilder producerFieldNoParentPat = producerFieldNoParent.pat();
    static TestCaseBuilder producerMethodNoParentPat = producerMethodNoParent.pat();
    static TestCaseBuilder producerDefaultMethodNoParentPat = producerDefaultMethodNoParent.pat();

    public static class FieldTest extends AbstractTest {
        public FieldTest() {
            super(producerField);
        }

        @Deployment
        public static Archive<?> deploy() {
            return producerField.deploy(FieldTest.class);
        }
    }

    public static class MethodTest extends AbstractTest {
        public MethodTest() {
            super(producerMethod);
        }

        @Deployment
        public static Archive<?> deploy() {
            return producerMethod.deploy(MethodTest.class);
        }
    }

    public static class DefaultMethodTest extends AbstractTest {
        public DefaultMethodTest() {
            super(producerDefaultMethod);
        }

        @Deployment
        public static Archive<?> deploy() {
            return producerDefaultMethod.deploy(DefaultMethodTest.class);
        }
    }

    public static class FieldNoParentTest extends AbstractTest {
        public FieldNoParentTest() {
            super(producerFieldNoParent);
        }

        @Deployment
        public static Archive<?> deploy() {
            return producerFieldNoParent.deploy(FieldNoParentTest.class);
        }
    }

    public static class MethodNoParentTest extends AbstractTest {
        public MethodNoParentTest() {
            super(producerMethodNoParent);
        }

        @Deployment
        public static Archive<?> deploy() {
            return producerMethodNoParent.deploy(MethodNoParentTest.class);
        }
    }

    public static class DefaultMethodNoParentTest extends AbstractTest {
        public DefaultMethodNoParentTest() {
            super(producerDefaultMethodNoParent);
        }

        @Deployment
        public static Archive<?> deploy() {
            return producerDefaultMethodNoParent.deploy(DefaultMethodNoParentTest.class);
        }
    }

    public static class FieldPatTest extends AbstractTest {
        public FieldPatTest() {
            super(producerFieldPat);
        }

        @Deployment
        public static Archive<?> deploy() {
            return producerFieldPat.deploy(FieldPatTest.class);
        }
    }

    public static class MethodPatTest extends AbstractTest {
        public MethodPatTest() {
            super(producerMethodPat);
        }

        @Deployment
        public static Archive<?> deploy() {
            return producerMethodPat.deploy(MethodPatTest.class);
        }
    }

    public static class DefaultMethodPatTest extends AbstractTest {
        public DefaultMethodPatTest() {
            super(producerDefaultMethodPat);
        }

        @Deployment
        public static Archive<?> deploy() {
            return producerDefaultMethodPat.deploy(DefaultMethodPatTest.class);
        }
    }

    public static class FieldNoParentPatTest extends AbstractTest {
        public FieldNoParentPatTest() {
            super(producerFieldNoParentPat);
        }

        @Deployment
        public static Archive<?> deploy() {
            return producerFieldNoParentPat.deploy(FieldNoParentPatTest.class);
        }
    }

    public static class MethodNoParentPatTest extends AbstractTest {
        public MethodNoParentPatTest() {
            super(producerMethodNoParentPat);
        }

        @Deployment
        public static Archive<?> deploy() {
            return producerMethodNoParentPat.deploy(MethodNoParentPatTest.class);
        }
    }

    public static class DefaultMethodNoParentPatTest extends AbstractTest {
        public DefaultMethodNoParentPatTest() {
            super(producerDefaultMethodNoParentPat);
        }

        @Deployment
        public static Archive<?> deploy() {
            return producerDefaultMethodNoParentPat.deploy(DefaultMethodNoParentPatTest.class);
        }
    }

}
