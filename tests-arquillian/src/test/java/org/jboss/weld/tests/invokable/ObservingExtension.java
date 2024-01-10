package org.jboss.weld.tests.invokable;

import java.util.Collection;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessManagedBean;
import jakarta.enterprise.invoke.Invoker;

import org.jboss.weld.invoke.WeldInvokerBuilder;
import org.jboss.weld.tests.invokable.common.ArgTransformer;
import org.jboss.weld.tests.invokable.common.ExceptionTransformer;
import org.jboss.weld.tests.invokable.common.FooArg;
import org.jboss.weld.tests.invokable.common.InstanceTransformer;
import org.jboss.weld.tests.invokable.common.InvocationWrapper;
import org.jboss.weld.tests.invokable.common.ReturnValueTransformer;
import org.jboss.weld.tests.invokable.common.SimpleBean;
import org.jboss.weld.tests.invokable.common.TransformableBean;
import org.jboss.weld.tests.invokable.common.TrulyExceptionalBean;
import org.junit.Assert;

public class ObservingExtension implements Extension {

    public Invoker<SimpleBean, ?> getNoTransformationInvoker() {
        return noTransformationInvoker;
    }

    public Invoker<SimpleBean, ?> getInstanceLookupInvoker() {
        return instanceLookupInvoker;
    }

    public Invoker<SimpleBean, ?> getArgLookupInvoker() {
        return argLookupInvoker;
    }

    public Invoker<SimpleBean, ?> getLookupAllInvoker() {
        return lookupAllInvoker;
    }

    public Invoker<SimpleBean, ?> getStaticNoTransformationInvoker() {
        return staticNoTransformationInvoker;
    }

    public Invoker<SimpleBean, ?> getStaticInstanceLookupInvoker() {
        return staticInstanceLookupInvoker;
    }

    public Invoker<SimpleBean, ?> getStaticArgLookupInvoker() {
        return staticArgLookupInvoker;
    }

    public Invoker<SimpleBean, ?> getStaticLookupAllInvoker() {
        return staticLookupAllInvoker;
    }

    public Invoker<TransformableBean, ?> getArgTransformingInvoker() {
        return argTransformingInvoker;
    }

    public Invoker<TransformableBean, ?> getStaticArgTransformingInvoker() {
        return staticArgTransformingInvoker;
    }

    public Invoker<TransformableBean, ?> getArgTransformerWithConsumerInvoker() {
        return argTransformerWithConsumerInvoker;
    }

    public Invoker<TransformableBean, ?> getStaticArgTransformerWithConsumerInvoker() {
        return staticArgTransformerWithConsumerInvoker;
    }

    public Invoker<TransformableBean, ?> getInstanceTransformerInvoker() {
        return instanceTransformerInvoker;
    }

    public Invoker<TransformableBean, ?> getInstanceTransformerWithConsumerInvoker() {
        return instanceTransformerWithConsumerInvoker;
    }

    public Invoker<TransformableBean, ?> getInstanceTransformerNoParamInvoker() {
        return instanceTransformerNoParamInvoker;
    }

    public Invoker<TransformableBean, ?> getReturnTransformerInvoker() {
        return returnTransformerInvoker;
    }

    public Invoker<TransformableBean, ?> getStaticReturnTransformerInvoker() {
        return staticReturnTransformerInvoker;
    }

    public Invoker<TransformableBean, ?> getReturnTransformerNoParamInvoker() {
        return returnTransformerNoParamInvoker;
    }

    public Invoker<TransformableBean, ?> getStaticReturnTransformerNoParamInvoker() {
        return staticReturnTransformerNoParamInvoker;
    }

    public Invoker<TrulyExceptionalBean, ?> getExceptionTransformerInvoker() {
        return exceptionTransformerInvoker;
    }

    public Invoker<TrulyExceptionalBean, ?> getStaticExceptionTransformerInvoker() {
        return staticExceptionTransformerInvoker;
    }

    public Invoker<SimpleBean, ?> getInvocationWrapperInvoker() {
        return invocationWrapperInvoker;
    }

    public Invoker<SimpleBean, ?> getStaticInvocationWrapperInvoker() {
        return staticInvocationWrapperInvoker;
    }

    // basic invokers, some with lookup
    private Invoker<SimpleBean, ?> noTransformationInvoker;
    private Invoker<SimpleBean, ?> instanceLookupInvoker;
    private Invoker<SimpleBean, ?> argLookupInvoker;
    private Invoker<SimpleBean, ?> lookupAllInvoker;
    private Invoker<SimpleBean, ?> staticNoTransformationInvoker;
    private Invoker<SimpleBean, ?> staticInstanceLookupInvoker;
    private Invoker<SimpleBean, ?> staticArgLookupInvoker;
    private Invoker<SimpleBean, ?> staticLookupAllInvoker;

    // method arg transformers
    private Invoker<TransformableBean, ?> argTransformingInvoker;
    private Invoker<TransformableBean, ?> staticArgTransformingInvoker;
    private Invoker<TransformableBean, ?> argTransformerWithConsumerInvoker;
    private Invoker<TransformableBean, ?> staticArgTransformerWithConsumerInvoker;

    // instance transformers
    private Invoker<TransformableBean, ?> instanceTransformerInvoker;
    private Invoker<TransformableBean, ?> instanceTransformerWithConsumerInvoker;
    private Invoker<TransformableBean, ?> instanceTransformerNoParamInvoker;

    // return value transformers
    private Invoker<TransformableBean, ?> returnTransformerInvoker;
    private Invoker<TransformableBean, ?> returnTransformerNoParamInvoker;
    private Invoker<TransformableBean, ?> staticReturnTransformerInvoker;
    private Invoker<TransformableBean, ?> staticReturnTransformerNoParamInvoker;

    // exception transformers
    private Invoker<TrulyExceptionalBean, ?> exceptionTransformerInvoker;
    private Invoker<TrulyExceptionalBean, ?> staticExceptionTransformerInvoker;

    // invocation wrapper
    private Invoker<SimpleBean, ?> invocationWrapperInvoker;
    private Invoker<SimpleBean, ?> staticInvocationWrapperInvoker;

    public void createNoTransformationInvokers(@Observes ProcessManagedBean<SimpleBean> pmb) {
        Collection<AnnotatedMethod<? super SimpleBean>> invokableMethods = pmb.getAnnotatedBeanClass().getMethods();
        Assert.assertEquals(4, invokableMethods.size());
        for (AnnotatedMethod<? super SimpleBean> invokableMethod : invokableMethods) {
            if (invokableMethod.getJavaMember().getName().contains("staticPing")) {
                staticNoTransformationInvoker = pmb.createInvoker(invokableMethod).build();
                staticInstanceLookupInvoker = pmb.createInvoker(invokableMethod).withInstanceLookup().build();
                staticArgLookupInvoker = pmb.createInvoker(invokableMethod).withArgumentLookup(0).withArgumentLookup(1).build();
                staticLookupAllInvoker = pmb.createInvoker(invokableMethod).withArgumentLookup(0).withArgumentLookup(1)
                        .withInstanceLookup().build();
            } else if (invokableMethod.getJavaMember().getName().contains("ping")) {
                noTransformationInvoker = pmb.createInvoker(invokableMethod).build();
                instanceLookupInvoker = pmb.createInvoker(invokableMethod).withInstanceLookup().build();
                argLookupInvoker = pmb.createInvoker(invokableMethod).withArgumentLookup(0).withArgumentLookup(1).build();
                lookupAllInvoker = pmb.createInvoker(invokableMethod).withArgumentLookup(0).withArgumentLookup(1)
                        .withInstanceLookup().build();
            }
        }
    }

    public void createArgTransformationInvokers(@Observes ProcessManagedBean<TransformableBean> pmb) {
        Collection<AnnotatedMethod<? super TransformableBean>> invokableMethods = pmb.getAnnotatedBeanClass().getMethods();
        Assert.assertEquals(4, invokableMethods.size());
        for (AnnotatedMethod<? super TransformableBean> invokableMethod : invokableMethods) {
            if (invokableMethod.getJavaMember().getName().contains("staticPing")) {
                staticArgTransformingInvoker = ((WeldInvokerBuilder<Invoker<TransformableBean, ?>>) pmb
                        .createInvoker(invokableMethod))
                        .withArgumentTransformer(0, FooArg.class, "doubleTheString") // non-static Transformer method
                        .withArgumentTransformer(1, ArgTransformer.class, "transform") // static Transformer method
                        .build();
                staticArgTransformerWithConsumerInvoker = ((WeldInvokerBuilder<Invoker<TransformableBean, ?>>) pmb
                        .createInvoker(invokableMethod))
                        .withArgumentTransformer(0, FooArg.class, "doubleTheString") // non-static Transformer method
                        .withArgumentTransformer(1, ArgTransformer.class, "transform2") // static Transformer method with Consumer
                        .build();
            } else if (invokableMethod.getJavaMember().getName().contains("ping")) {
                argTransformingInvoker = ((WeldInvokerBuilder<Invoker<TransformableBean, ?>>) pmb
                        .createInvoker(invokableMethod))
                        .withArgumentTransformer(0, FooArg.class, "doubleTheString") // non-static Transformer method
                        .withArgumentTransformer(1, ArgTransformer.class, "transform") // static Transformer method
                        .build();
                argTransformerWithConsumerInvoker = ((WeldInvokerBuilder<Invoker<TransformableBean, ?>>) pmb
                        .createInvoker(invokableMethod))
                        .withArgumentTransformer(0, FooArg.class, "doubleTheString") // non-static Transformer method
                        .withArgumentTransformer(1, ArgTransformer.class, "transform2") // static Transformer method with Consumer
                        .build();
            }
        }
    }

    public void createInstanceTransformationInvokers(@Observes ProcessManagedBean<TransformableBean> pmb) {
        Collection<AnnotatedMethod<? super TransformableBean>> invokableMethods = pmb.getAnnotatedBeanClass().getMethods();
        Assert.assertEquals(4, invokableMethods.size());
        for (AnnotatedMethod<? super TransformableBean> invokableMethod : invokableMethods) {
            if (invokableMethod.getJavaMember().getName().contains("ping")) {
                instanceTransformerInvoker = ((WeldInvokerBuilder<Invoker<TransformableBean, ?>>) pmb
                        .createInvoker(invokableMethod))
                        .withInstanceTransformer(InstanceTransformer.class, "transform")
                        .build();
                instanceTransformerWithConsumerInvoker = ((WeldInvokerBuilder<Invoker<TransformableBean, ?>>) pmb
                        .createInvoker(invokableMethod))
                        .withInstanceTransformer(InstanceTransformer.class, "transform2")
                        .build();
                instanceTransformerNoParamInvoker = ((WeldInvokerBuilder<Invoker<TransformableBean, ?>>) pmb
                        .createInvoker(invokableMethod))
                        .withInstanceTransformer(TransformableBean.class, "setTransformed")
                        .build();
            }
        }
    }

    public void createReturnValueTransformationInvokers(@Observes ProcessManagedBean<TransformableBean> pmb) {
        Collection<AnnotatedMethod<? super TransformableBean>> invokableMethods = pmb.getAnnotatedBeanClass().getMethods();
        Assert.assertEquals(4, invokableMethods.size());
        for (AnnotatedMethod<? super TransformableBean> invokableMethod : invokableMethods) {
            if (invokableMethod.getJavaMember().getName().contains("ping")) {
                returnTransformerInvoker = ((WeldInvokerBuilder<Invoker<TransformableBean, ?>>) pmb
                        .createInvoker(invokableMethod))
                        .withReturnValueTransformer(ReturnValueTransformer.class, "transform")
                        .build();
                returnTransformerNoParamInvoker = ((WeldInvokerBuilder<Invoker<TransformableBean, ?>>) pmb
                        .createInvoker(invokableMethod))
                        .withReturnValueTransformer(String.class, "strip")
                        .build();

            } else if (invokableMethod.getJavaMember().getName().contains("staticPing")) {
                staticReturnTransformerInvoker = ((WeldInvokerBuilder<Invoker<TransformableBean, ?>>) pmb
                        .createInvoker(invokableMethod))
                        .withReturnValueTransformer(ReturnValueTransformer.class, "transform")
                        .build();
                staticReturnTransformerNoParamInvoker = ((WeldInvokerBuilder<Invoker<TransformableBean, ?>>) pmb
                        .createInvoker(invokableMethod))
                        .withReturnValueTransformer(String.class, "strip")
                        .build();
            }
        }
    }

    public void createExceptionTransformationInvokers(@Observes ProcessManagedBean<TrulyExceptionalBean> pmb) {
        Collection<AnnotatedMethod<? super TrulyExceptionalBean>> invokableMethods = pmb.getAnnotatedBeanClass().getMethods();
        Assert.assertEquals(2, invokableMethods.size());
        for (AnnotatedMethod<? super TrulyExceptionalBean> invokableMethod : invokableMethods) {
            if (invokableMethod.getJavaMember().getName().contains("ping")) {
                exceptionTransformerInvoker = ((WeldInvokerBuilder<Invoker<TrulyExceptionalBean, ?>>) pmb
                        .createInvoker(invokableMethod))
                        .withExceptionTransformer(ExceptionTransformer.class, "transform")
                        .build();

            } else if (invokableMethod.getJavaMember().getName().contains("staticPing")) {
                staticExceptionTransformerInvoker = ((WeldInvokerBuilder<Invoker<TrulyExceptionalBean, ?>>) pmb
                        .createInvoker(invokableMethod))
                        .withExceptionTransformer(ExceptionTransformer.class, "transform")
                        .build();
            }
        }
    }

    public void createInvocationWrapperInvokers(@Observes ProcessManagedBean<SimpleBean> pmb) {
        Collection<AnnotatedMethod<? super SimpleBean>> invokableMethods = pmb.getAnnotatedBeanClass().getMethods();
        Assert.assertEquals(4, invokableMethods.size());
        for (AnnotatedMethod<? super SimpleBean> invokableMethod : invokableMethods) {
            if (invokableMethod.getJavaMember().getName().contains("ping")) {
                invocationWrapperInvoker = ((WeldInvokerBuilder<Invoker<SimpleBean, ?>>) pmb.createInvoker(invokableMethod))
                        .withInvocationWrapper(InvocationWrapper.class, "transform")
                        .build();

            } else if (invokableMethod.getJavaMember().getName().contains("staticPing")) {
                staticInvocationWrapperInvoker = ((WeldInvokerBuilder<Invoker<SimpleBean, ?>>) pmb
                        .createInvoker(invokableMethod))
                        .withInvocationWrapper(InvocationWrapper.class, "transform")
                        .build();
            }
        }
    }

}
