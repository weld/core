package org.jboss.weld.tests.invokable.exceptions;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessManagedBean;
import jakarta.enterprise.invoke.Invoker;

public class ObservingExtension implements Extension {

    private Invoker<ExceptionTestBean, ?> pingInvoker;
    private Invoker<ExceptionTestBean, ?> staticPingInvoker;
    private Invoker<ExceptionTestBean, ?> voidPingInvoker;
    private Invoker<ExceptionTestBean, ?> noargPingInvoker;

    public Invoker<ExceptionTestBean, ?> getPingInvoker() {
        return pingInvoker;
    }

    public Invoker<ExceptionTestBean, ?> getStaticPingInvoker() {
        return staticPingInvoker;
    }

    public Invoker<ExceptionTestBean, ?> getVoidPingInvoker() {
        return voidPingInvoker;
    }

    public Invoker<ExceptionTestBean, ?> getNoargPingInvoker() {
        return noargPingInvoker;
    }

    public void createInvoker(@Observes ProcessManagedBean<ExceptionTestBean> pmb) {
        AnnotatedMethod<? super ExceptionTestBean> pingMethod = getMethod(pmb, "ping");
        pingInvoker = pmb.createInvoker(pingMethod).build();

        AnnotatedMethod<? super ExceptionTestBean> staticPingMethod = getMethod(pmb, "staticPing");
        staticPingInvoker = pmb.createInvoker(staticPingMethod).build();

        AnnotatedMethod<? super ExceptionTestBean> voidPingMethod = getMethod(pmb, "voidPing");
        voidPingInvoker = pmb.createInvoker(voidPingMethod).build();

        AnnotatedMethod<? super ExceptionTestBean> noargPingMethod = getMethod(pmb, "noargPing");
        noargPingInvoker = pmb.createInvoker(noargPingMethod).build();
    }

    private <T> AnnotatedMethod<? super T> getMethod(ProcessManagedBean<T> event, String methodName) {
        return event.getAnnotatedBeanClass().getMethods().stream()
                .filter(m -> m.getJavaMember().getName().equals(methodName)).findFirst().get();
    }
}
