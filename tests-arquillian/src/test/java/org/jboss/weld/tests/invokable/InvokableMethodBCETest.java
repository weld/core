package org.jboss.weld.tests.invokable;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.invokable.common.ArgTransformer;
import org.jboss.weld.tests.invokable.common.FooArg;
import org.jboss.weld.tests.invokable.common.HelperBean;
import org.jboss.weld.tests.invokable.common.InstanceTransformer;
import org.jboss.weld.tests.invokable.common.SimpleBean;
import org.jboss.weld.tests.invokable.common.TransformableBean;
import org.jboss.weld.tests.invokable.common.TrulyExceptionalBean;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class InvokableMethodBCETest {

    @Deployment
    public static Archive getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InvokableMethodBCETest.class))
                .addPackage(InvokableMethodBCETest.class.getPackage())
                .addPackage(SimpleBean.class.getPackage())
                .addAsServiceProvider(BuildCompatibleExtension.class, BuildCompatExtension.class);
    }

    @Inject
    SimpleBean simpleBean;

    @Inject
    TransformableBean transformableBean;

    @Inject
    TrulyExceptionalBean trulyExceptionalBean;

    @Inject
    SynthBean synthBean;

    @Test
    public void testSimpleInvokableMethod() {
        SimpleBean.resetDestroyCounter();
        HelperBean.clearDestroyedCounters();

        // no transformation invocation, no lookup
        Assert.assertEquals("foo1", synthBean.getNoTransformationInvoker().invoke(simpleBean, new Object[]{"foo", 1}));
        Assert.assertEquals(0, SimpleBean.preDestroyInvoked);
        Assert.assertEquals(0, HelperBean.timesStringDestroyed);
        Assert.assertEquals(0, HelperBean.timesIntDestroyed);

        // no transformation, instance lookup
        Assert.assertEquals("foo1", synthBean.getInstanceLookupInvoker().invoke(null, new Object[]{"foo", 1}));
        Assert.assertEquals(1, SimpleBean.preDestroyInvoked);
        Assert.assertEquals(0, HelperBean.timesStringDestroyed);
        Assert.assertEquals(0, HelperBean.timesIntDestroyed);

        // no transformation, arg lookup (all)
        Assert.assertEquals("bar42", synthBean.getArgLookupInvoker().invoke(simpleBean, new Object[]{"blah", null}));
        Assert.assertEquals(1, SimpleBean.preDestroyInvoked);
        Assert.assertEquals(1, HelperBean.timesStringDestroyed);
        Assert.assertEquals(1, HelperBean.timesIntDestroyed);

        // no transformation, instance lookup and arg lookup (all)
        Assert.assertEquals("bar42", synthBean.getLookupAllInvoker().invoke(new SimpleBean(), new Object[]{"blah", null}));
        Assert.assertEquals(2, SimpleBean.preDestroyInvoked);
        Assert.assertEquals(2, HelperBean.timesStringDestroyed);
        Assert.assertEquals(2, HelperBean.timesIntDestroyed);    }

    @Test
    public void testSimpleStaticInvokableMethod() {
        SimpleBean.resetDestroyCounter();
        HelperBean.clearDestroyedCounters();

        // no transformation invocation, no lookup
        Assert.assertEquals("foo1", synthBean.getStaticNoTransformationInvoker().invoke(simpleBean, new Object[]{"foo", 1}));
        Assert.assertEquals(0, SimpleBean.preDestroyInvoked);
        Assert.assertEquals(0, HelperBean.timesStringDestroyed);
        Assert.assertEquals(0, HelperBean.timesIntDestroyed);

        // no transformation, no instance lookup (configured but skipped, it is a static method)
        Assert.assertEquals("foo1", synthBean.getStaticInstanceLookupInvoker().invoke(null, new Object[]{"foo", 1}));
        Assert.assertEquals(0, SimpleBean.preDestroyInvoked);
        Assert.assertEquals(0, HelperBean.timesStringDestroyed);
        Assert.assertEquals(0, HelperBean.timesIntDestroyed);

        // no transformation, arg lookup (all)
        Assert.assertEquals("bar42", synthBean.getStaticArgLookupInvoker().invoke(simpleBean, new Object[]{"blah", null}));
        Assert.assertEquals(0, SimpleBean.preDestroyInvoked);
        Assert.assertEquals(1, HelperBean.timesStringDestroyed);
        Assert.assertEquals(1, HelperBean.timesIntDestroyed);

        // no transformation, no instance lookup (configured but skipped) and arg lookup (all)
        Assert.assertEquals("bar42", synthBean.getStaticLookupAllInvoker().invoke(new SimpleBean(), new Object[]{"blah", null}));
        Assert.assertEquals(0, SimpleBean.preDestroyInvoked);
        Assert.assertEquals(2, HelperBean.timesStringDestroyed);
        Assert.assertEquals(2, HelperBean.timesIntDestroyed);
    }

    @Test
    public void testArgTransformingInvokableMethod() {
        ArgTransformer.runnableExecuted = 0;

        String fooArg = "fooArg";
        String expected = fooArg + fooArg + ArgTransformer.transformed;
        Assert.assertEquals(expected, synthBean.getArgTransformingInvoker().invoke(transformableBean, new Object[]{new FooArg(fooArg), "bar"}));
        Assert.assertEquals(expected, synthBean.getStaticArgTransformingInvoker().invoke(null, new Object[]{new FooArg(fooArg), "bar"}));

        // transformer with Consumer<Runnable> parameter
        Assert.assertEquals(0, ArgTransformer.runnableExecuted);
        Assert.assertEquals(expected, synthBean.getArgTransformerWithConsumerInvoker().invoke(transformableBean, new Object[]{new FooArg(fooArg), "bar"}));
        Assert.assertEquals(1, ArgTransformer.runnableExecuted);
        Assert.assertEquals(expected, synthBean.getStaticArgTransformerWithConsumerInvoker().invoke(null, new Object[]{new FooArg(fooArg), "bar"}));
        Assert.assertEquals(2, ArgTransformer.runnableExecuted);
    }

    @Test
    public void testInstanceTransformingInvokableMethod() {
        InstanceTransformer.runnableExecuted = 0;

        //test is intentionally *NOT* using the bean, but instead passes in new instance every time
        String fooArg = "fooArg";
        String expected = (fooArg + fooArg).toUpperCase();
        TransformableBean targetInstance;

        // transformer defined on other class
        targetInstance = new TransformableBean();
        Assert.assertFalse(targetInstance.isTransformed());
        Assert.assertEquals(expected, synthBean.getInstanceTransformerInvoker().invoke(targetInstance, new Object[]{new FooArg(fooArg), fooArg}));
        Assert.assertTrue(targetInstance.isTransformed());

        // transformer defined on other class with Consumer<Runnable> param
        targetInstance = new TransformableBean();
        Assert.assertFalse(targetInstance.isTransformed());
        Assert.assertEquals(0, InstanceTransformer.runnableExecuted);
        Assert.assertEquals(expected, synthBean.getInstanceTransformerWithConsumerInvoker().invoke(targetInstance, new Object[]{new FooArg(fooArg), fooArg}));
        Assert.assertEquals(1, InstanceTransformer.runnableExecuted);
        Assert.assertTrue(targetInstance.isTransformed());

        // transformer on the bean class, no arg method
        targetInstance = new TransformableBean();
        Assert.assertFalse(targetInstance.isTransformed());
        Assert.assertEquals(expected, synthBean.getInstanceTransformerNoParamInvoker().invoke(targetInstance, new Object[]{new FooArg(fooArg), fooArg}));
        Assert.assertTrue(targetInstance.isTransformed());
    }

    @Test
    public void testReturnValueTransformingInvokableMethod() {
        String fooArg = "  fooArg  ";
        String expected = (fooArg + fooArg).strip();

        // transformer defined on other class
        Assert.assertEquals(expected, synthBean.getReturnTransformerInvoker().invoke(transformableBean, new Object[]{new FooArg(fooArg), fooArg}));
        Assert.assertEquals(expected, synthBean.getStaticReturnTransformerInvoker().invoke(transformableBean, new Object[]{new FooArg(fooArg), fooArg}));

        // transformer on the result class (String), no arg method
        Assert.assertEquals(expected, synthBean.getReturnTransformerNoParamInvoker().invoke(transformableBean, new Object[]{new FooArg(fooArg), fooArg}));
        Assert.assertEquals(expected, synthBean.getStaticReturnTransformerNoParamInvoker().invoke(transformableBean, new Object[]{new FooArg(fooArg), fooArg}));
    }

    @Test
    public void testExceptionTransformingInvokableMethod() {
        String expected = IllegalArgumentException.class.getSimpleName();
        String expectedStatic = IllegalStateException.class.getSimpleName();

        // exception transformer can only be defined on other class
        Assert.assertEquals(expected, synthBean.getExceptionTransformerInvoker().invoke(trulyExceptionalBean, new Object[]{"foo", 42}));
        Assert.assertEquals(expectedStatic, synthBean.getStaticExceptionTransformerInvoker().invoke(trulyExceptionalBean, new Object[]{"foo", 42}));
    }

    @Test
    public void testInvocationWrapperInvokableMethod() {
        String expected = "foo42foo42";

        // invocation wrapper defined on other class
        Assert.assertEquals(expected, synthBean.getInvocationWrapperInvoker().invoke(simpleBean, new Object[]{"foo", 42}));
        Assert.assertEquals(expected, synthBean.getStaticInvocationWrapperInvoker().invoke(simpleBean, new Object[]{"foo", 42}));
    }
}
