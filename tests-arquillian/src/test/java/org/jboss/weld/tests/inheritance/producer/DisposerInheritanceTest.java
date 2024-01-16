package org.jboss.weld.tests.inheritance.producer;

import java.util.function.Consumer;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.inject.Inject;

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
public class DisposerInheritanceTest {

    abstract static class Foo {
        boolean isDisposed;
    }

    interface Parent {
    }

    interface Child {
    }

    @Dependent
    static class MethodParent implements Parent {
        @Produces
        Foo foo() {
            return new Foo() {
            };
        }

        void disposeFoo(@Disposes Foo foo) {
            foo.isDisposed = true;
        }
    }

    @Dependent
    static class MethodChild extends MethodParent implements Child {
    }

    interface DefaultMethodParent extends Parent {
        default void disposeFoo(@Disposes Foo foo) {
            foo.isDisposed = true;
        }
    }

    @Dependent
    static class DefaultMethodChild implements DefaultMethodParent, Child {
        @Produces
        Foo foo() {
            return new Foo() {
            };
        }
    }

    public static class ProcessAnnotatedTypeExtension implements Extension {
        <T extends Parent> void processAnnotatedType(@Observes ProcessAnnotatedType<T> processAnnotatedType) {
            // calling configureAnnotatedType() appears to be sufficient for the AnnotatedType pat.getAnnotatedType()
            // to get replaced after this method processAnnotatedType has returned with (zero or more) changes applied.
            processAnnotatedType.configureAnnotatedType();
        }
    }

    static final Consumer<Foo> assertDisposed = foo -> Assert.assertTrue(foo.isDisposed);
    static final Consumer<Foo> assertNotDisposed = foo -> Assert.assertFalse(foo.isDisposed);

    static class TestCaseBuilder implements Cloneable {
        Class<?> testClass;
        Class<?> parentClass;
        Class<?> childClass;
        boolean deployProcessAnnotatedTypeObserver;
        Consumer<Foo> assertion = assertDisposed;

        protected TestCaseBuilder clone() {
            try {
                return (TestCaseBuilder) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        TestCaseBuilder parent(Class<?> parentClass) {
            TestCaseBuilder builder = clone();
            builder.parentClass = parentClass;
            return builder;
        }

        TestCaseBuilder child(Class<?> childClass) {
            TestCaseBuilder builder = clone();
            builder.childClass = childClass;
            return builder;
        }

        TestCaseBuilder pat() {
            TestCaseBuilder builder = clone();
            builder.deployProcessAnnotatedTypeObserver = true;
            return builder;
        }

        TestCaseBuilder disposesFoo(boolean disposes) {
            TestCaseBuilder builder = clone();
            builder.assertion = disposes ? assertDisposed : assertNotDisposed;
            return builder;
        }

        JavaArchive deploy(Class<?> testClass) {
            this.testClass = testClass;
            JavaArchive archive = ShrinkWrap
                    .create(BeanArchive.class, Utils.getDeploymentNameAsHash(testClass))
                    .addClass(testClass);
            archive = archive.addClass(parentClass);
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

        @Inject
        BeanManager beanManager;

        @Test
        public void testDisposer() {
            Bean<Foo> bean = Utils.getBean(beanManager, Foo.class, Default.Literal.INSTANCE);
            CreationalContext<Foo> ctx = beanManager.createCreationalContext(bean);
            Foo foo = bean.create(ctx);
            assertNotDisposed.accept(foo);

            bean.destroy(foo, ctx);
            testCaseBuilder.assertion.accept(foo);
        }
    }

    static TestCaseBuilder disposerMethod = new TestCaseBuilder().parent(MethodParent.class).child(MethodChild.class);

    /**
     * {@code foo} cannot be resolved because neither {@link Parent} nor {@link Child} dispose {@link Foo}.
     * Both {@link Parent} and {@link Child}'s disposer methods are considered inherited and therefore both
     * don't actually dispose.
     */
    static TestCaseBuilder disposerDefaultMethod = new TestCaseBuilder().parent(DefaultMethodParent.class)
            .child(DefaultMethodChild.class).disposesFoo(false);
    static TestCaseBuilder disposerMethodPat = disposerMethod.pat();
    static TestCaseBuilder disposerDefaultMethodPat = disposerDefaultMethod.pat();

    public static class MethodTest extends AbstractTest {
        public MethodTest() {
            super(disposerMethod);
        }

        @Deployment
        public static Archive<?> deploy() {
            return disposerMethod.deploy(MethodTest.class);
        }
    }

    public static class DefaultMethodTest extends AbstractTest {
        public DefaultMethodTest() {
            super(disposerDefaultMethod);
        }

        @Deployment
        public static Archive<?> deploy() {
            return disposerDefaultMethod.deploy(DefaultMethodTest.class);
        }
    }

    public static class MethodPatTest extends AbstractTest {
        public MethodPatTest() {
            super(disposerMethodPat);
        }

        @Deployment
        public static Archive<?> deploy() {
            return disposerMethodPat.deploy(MethodPatTest.class);
        }
    }

    public static class DefaultMethodPatTest extends AbstractTest {
        public DefaultMethodPatTest() {
            super(disposerDefaultMethodPat);
        }

        @Deployment
        public static Archive<?> deploy() {
            return disposerDefaultMethodPat.deploy(DefaultMethodPatTest.class);
        }
    }

}
