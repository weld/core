package org.jboss.weld.tests.invokable;

import jakarta.enterprise.invoke.Invoker;

import org.jboss.weld.tests.invokable.common.SimpleBean;
import org.jboss.weld.tests.invokable.common.TransformableBean;
import org.jboss.weld.tests.invokable.common.TrulyExceptionalBean;

/**
 * Bean registered via BCE
 */
public class SynthBean {
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

    public void setNoTransformationInvoker(Invoker<SimpleBean, ?> noTransformationInvoker) {
        this.noTransformationInvoker = noTransformationInvoker;
    }

    public void setInstanceLookupInvoker(Invoker<SimpleBean, ?> instanceLookupInvoker) {
        this.instanceLookupInvoker = instanceLookupInvoker;
    }

    public void setArgLookupInvoker(Invoker<SimpleBean, ?> argLookupInvoker) {
        this.argLookupInvoker = argLookupInvoker;
    }

    public void setLookupAllInvoker(Invoker<SimpleBean, ?> lookupAllInvoker) {
        this.lookupAllInvoker = lookupAllInvoker;
    }

    public void setStaticNoTransformationInvoker(Invoker<SimpleBean, ?> staticNoTransformationInvoker) {
        this.staticNoTransformationInvoker = staticNoTransformationInvoker;
    }

    public void setStaticInstanceLookupInvoker(Invoker<SimpleBean, ?> staticInstanceLookupInvoker) {
        this.staticInstanceLookupInvoker = staticInstanceLookupInvoker;
    }

    public void setStaticArgLookupInvoker(Invoker<SimpleBean, ?> staticArgLookupInvoker) {
        this.staticArgLookupInvoker = staticArgLookupInvoker;
    }

    public void setStaticLookupAllInvoker(Invoker<SimpleBean, ?> staticLookupAllInvoker) {
        this.staticLookupAllInvoker = staticLookupAllInvoker;
    }

    public void setArgTransformingInvoker(Invoker<TransformableBean, ?> argTransformingInvoker) {
        this.argTransformingInvoker = argTransformingInvoker;
    }

    public void setStaticArgTransformingInvoker(Invoker<TransformableBean, ?> staticArgTransformingInvoker) {
        this.staticArgTransformingInvoker = staticArgTransformingInvoker;
    }

    public void setArgTransformerWithConsumerInvoker(Invoker<TransformableBean, ?> argTransformerWithConsumerInvoker) {
        this.argTransformerWithConsumerInvoker = argTransformerWithConsumerInvoker;
    }

    public void setStaticArgTransformerWithConsumerInvoker(
            Invoker<TransformableBean, ?> staticArgTransformerWithConsumerInvoker) {
        this.staticArgTransformerWithConsumerInvoker = staticArgTransformerWithConsumerInvoker;
    }

    public void setInstanceTransformerInvoker(Invoker<TransformableBean, ?> instanceTransformerInvoker) {
        this.instanceTransformerInvoker = instanceTransformerInvoker;
    }

    public void setInstanceTransformerWithConsumerInvoker(
            Invoker<TransformableBean, ?> instanceTransformerWithConsumerInvoker) {
        this.instanceTransformerWithConsumerInvoker = instanceTransformerWithConsumerInvoker;
    }

    public void setInstanceTransformerNoParamInvoker(Invoker<TransformableBean, ?> instanceTransformerNoParamInvoker) {
        this.instanceTransformerNoParamInvoker = instanceTransformerNoParamInvoker;
    }

    public void setReturnTransformerInvoker(Invoker<TransformableBean, ?> returnTransformerInvoker) {
        this.returnTransformerInvoker = returnTransformerInvoker;
    }

    public void setReturnTransformerNoParamInvoker(Invoker<TransformableBean, ?> returnTransformerNoParamInvoker) {
        this.returnTransformerNoParamInvoker = returnTransformerNoParamInvoker;
    }

    public void setStaticReturnTransformerInvoker(Invoker<TransformableBean, ?> staticReturnTransformerInvoker) {
        this.staticReturnTransformerInvoker = staticReturnTransformerInvoker;
    }

    public void setStaticReturnTransformerNoParamInvoker(Invoker<TransformableBean, ?> staticReturnTransformerNoParamInvoker) {
        this.staticReturnTransformerNoParamInvoker = staticReturnTransformerNoParamInvoker;
    }

    public void setExceptionTransformerInvoker(Invoker<TrulyExceptionalBean, ?> exceptionTransformerInvoker) {
        this.exceptionTransformerInvoker = exceptionTransformerInvoker;
    }

    public void setStaticExceptionTransformerInvoker(Invoker<TrulyExceptionalBean, ?> staticExceptionTransformerInvoker) {
        this.staticExceptionTransformerInvoker = staticExceptionTransformerInvoker;
    }

    public void setInvocationWrapperInvoker(Invoker<SimpleBean, ?> invocationWrapperInvoker) {
        this.invocationWrapperInvoker = invocationWrapperInvoker;
    }

    public void setStaticInvocationWrapperInvoker(Invoker<SimpleBean, ?> staticInvocationWrapperInvoker) {
        this.staticInvocationWrapperInvoker = staticInvocationWrapperInvoker;
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
}
