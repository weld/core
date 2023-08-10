package org.jboss.weld.tests.invokable;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessManagedBean;
import jakarta.enterprise.invoke.Invoker;
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

import java.util.Collection;

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
        Collection<AnnotatedMethod<? super SimpleBean>> invokableMethods = pmb.getInvokableMethods();
        Assert.assertEquals(2, invokableMethods.size());
        for (AnnotatedMethod<? super SimpleBean> invokableMethod : invokableMethods) {
            if (invokableMethod.getJavaMember().getName().contains("staticPing")) {
                staticNoTransformationInvoker = pmb.createInvoker(invokableMethod).build();
                staticInstanceLookupInvoker = pmb.createInvoker(invokableMethod).setInstanceLookup().build();
                staticArgLookupInvoker = pmb.createInvoker(invokableMethod).setArgumentLookup(0).setArgumentLookup(1).build();
                staticLookupAllInvoker = pmb.createInvoker(invokableMethod).setArgumentLookup(0).setArgumentLookup(1).setInstanceLookup().build();
            } else {
                noTransformationInvoker = pmb.createInvoker(invokableMethod).build();
                instanceLookupInvoker = pmb.createInvoker(invokableMethod).setInstanceLookup().build();
                argLookupInvoker = pmb.createInvoker(invokableMethod).setArgumentLookup(0).setArgumentLookup(1).build();
                lookupAllInvoker = pmb.createInvoker(invokableMethod).setArgumentLookup(0).setArgumentLookup(1).setInstanceLookup().build();
            }
        }
    }

    public void createArgTransformationInvokers(@Observes ProcessManagedBean<TransformableBean> pmb) {
        Collection<AnnotatedMethod<? super TransformableBean>> invokableMethods = pmb.getInvokableMethods();
        Assert.assertEquals(2, invokableMethods.size());
        for (AnnotatedMethod<? super TransformableBean> invokableMethod : invokableMethods) {
            if (invokableMethod.getJavaMember().getName().contains("staticPing")) {
                staticArgTransformingInvoker = pmb.createInvoker(invokableMethod)
                        .setArgumentTransformer(0, FooArg.class, "doubleTheString") // non-static transformer method
                        .setArgumentTransformer(1, ArgTransformer.class, "transform") // static transformer method
                        .build();
                staticArgTransformerWithConsumerInvoker = pmb.createInvoker(invokableMethod)
                        .setArgumentTransformer(0, FooArg.class, "doubleTheString") // non-static transformer method
                        .setArgumentTransformer(1, ArgTransformer.class, "transform2") // static transformer method with Consumer
                        .build();
            } else {
                argTransformingInvoker = pmb.createInvoker(invokableMethod)
                        .setArgumentTransformer(0, FooArg.class, "doubleTheString") // non-static transformer method
                        .setArgumentTransformer(1, ArgTransformer.class, "transform") // static transformer method
                        .build();
                argTransformerWithConsumerInvoker = pmb.createInvoker(invokableMethod)
                        .setArgumentTransformer(0, FooArg.class, "doubleTheString") // non-static transformer method
                        .setArgumentTransformer(1, ArgTransformer.class, "transform2") // static transformer method with Consumer
                        .build();
            }
        }
    }

    public void createInstanceTransformationInvokers(@Observes ProcessManagedBean<TransformableBean> pmb) {
        Collection<AnnotatedMethod<? super TransformableBean>> invokableMethods = pmb.getInvokableMethods();
        Assert.assertEquals(2, invokableMethods.size());
        for (AnnotatedMethod<? super TransformableBean> invokableMethod : invokableMethods) {
            if (invokableMethod.getJavaMember().getName().contains("ping")) {
                instanceTransformerInvoker = pmb.createInvoker(invokableMethod)
                        .setInstanceTransformer(InstanceTransformer.class, "transform")
                        .build();
                instanceTransformerWithConsumerInvoker = pmb.createInvoker(invokableMethod)
                        .setInstanceTransformer(InstanceTransformer.class, "transform2")
                        .build();
                instanceTransformerNoParamInvoker = pmb.createInvoker(invokableMethod)
                        .setInstanceTransformer(TransformableBean.class, "setTransformed")
                        .build();
            }
        }
    }

    public void createReturnValueTransformationInvokers(@Observes ProcessManagedBean<TransformableBean> pmb) {
        Collection<AnnotatedMethod<? super TransformableBean>> invokableMethods = pmb.getInvokableMethods();
        Assert.assertEquals(2, invokableMethods.size());
        for (AnnotatedMethod<? super TransformableBean> invokableMethod : invokableMethods) {
            if (invokableMethod.getJavaMember().getName().contains("ping")) {
                returnTransformerInvoker = pmb.createInvoker(invokableMethod)
                        .setReturnValueTransformer(ReturnValueTransformer.class, "transform")
                        .build();
                returnTransformerNoParamInvoker = pmb.createInvoker(invokableMethod)
                        .setReturnValueTransformer(String.class, "strip")
                        .build();

            } else {
                staticReturnTransformerInvoker = pmb.createInvoker(invokableMethod)
                        .setReturnValueTransformer(ReturnValueTransformer.class, "transform")
                        .build();
                staticReturnTransformerNoParamInvoker = pmb.createInvoker(invokableMethod)
                        .setReturnValueTransformer(String.class, "strip")
                        .build();
            }
        }
    }

    public void createExceptionTransformationInvokers(@Observes ProcessManagedBean<TrulyExceptionalBean> pmb) {
        Collection<AnnotatedMethod<? super TrulyExceptionalBean>> invokableMethods = pmb.getInvokableMethods();
        Assert.assertEquals(2, invokableMethods.size());
        for (AnnotatedMethod<? super TrulyExceptionalBean> invokableMethod : invokableMethods) {
            if (invokableMethod.getJavaMember().getName().contains("ping")) {
                exceptionTransformerInvoker = pmb.createInvoker(invokableMethod)
                        .setExceptionTransformer(ExceptionTransformer.class, "transform")
                        .build();

            } else {
                staticExceptionTransformerInvoker = pmb.createInvoker(invokableMethod)
                        .setExceptionTransformer(ExceptionTransformer.class, "transform")
                        .build();
            }
        }
    }

    public void createInvocationWrapperInvokers(@Observes ProcessManagedBean<SimpleBean> pmb) {
        Collection<AnnotatedMethod<? super SimpleBean>> invokableMethods = pmb.getInvokableMethods();
        Assert.assertEquals(2, invokableMethods.size());
        for (AnnotatedMethod<? super SimpleBean> invokableMethod : invokableMethods) {
            if (invokableMethod.getJavaMember().getName().contains("ping")) {
                invocationWrapperInvoker = pmb.createInvoker(invokableMethod)
                        .setInvocationWrapper(InvocationWrapper.class, "transform")
                        .build();

            } else {
                staticInvocationWrapperInvoker = pmb.createInvoker(invokableMethod)
                        .setInvocationWrapper(InvocationWrapper.class, "transform")
                        .build();
            }
        }
    }

}
