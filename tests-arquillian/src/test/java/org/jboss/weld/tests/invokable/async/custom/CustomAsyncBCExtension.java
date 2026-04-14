package org.jboss.weld.tests.invokable.async.custom;

import java.util.Collection;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.BeanInfo;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.InvokerFactory;
import jakarta.enterprise.inject.build.compatible.spi.InvokerInfo;
import jakarta.enterprise.inject.build.compatible.spi.InvokerValidation;
import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.Registration;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;
import jakarta.enterprise.inject.build.compatible.spi.Validation;
import jakarta.enterprise.invoke.Invoker;
import jakarta.enterprise.lang.model.declarations.MethodInfo;

public class CustomAsyncBCExtension implements BuildCompatibleExtension {

    private InvokerInfo invokerInfo;

    @Registration(types = CustomAsyncBean.class)
    public void register(BeanInfo bean, InvokerFactory invokers) {
        Collection<MethodInfo> methods = bean.declaringClass().methods();
        for (MethodInfo m : methods) {
            if ("hello".equals(m.name())) {
                invokerInfo = invokers.createInvoker(bean, m)
                        .withInstanceLookup()
                        .withArgumentLookup(0)
                        .build();
            }
        }
    }

    @Synthesis
    public void synthesis(SyntheticComponents syn) {
        syn.addBean(AsyncInvokerHolder.class)
                .type(AsyncInvokerHolder.class)
                .scope(ApplicationScoped.class)
                .createWith(AsyncInvokerHolderCreator.class)
                .withParam("invoker", invokerInfo);
    }

    @Validation
    public void validation(InvokerValidation invokers) {
        invokers.ensureAsyncHandlerExists(MyAsyncType.class);
    }

    public static class AsyncInvokerHolder {
        private Invoker<CustomAsyncBean, ?> invoker;

        public Invoker<CustomAsyncBean, ?> getInvoker() {
            return invoker;
        }

        public void setInvoker(Invoker<CustomAsyncBean, ?> invoker) {
            this.invoker = invoker;
        }
    }

    public static class AsyncInvokerHolderCreator implements SyntheticBeanCreator<AsyncInvokerHolder> {
        @Override
        @SuppressWarnings("unchecked")
        public AsyncInvokerHolder create(Instance<Object> lookup, Parameters params) {
            AsyncInvokerHolder holder = new AsyncInvokerHolder();
            holder.setInvoker(params.get("invoker", Invoker.class));
            return holder;
        }
    }
}
