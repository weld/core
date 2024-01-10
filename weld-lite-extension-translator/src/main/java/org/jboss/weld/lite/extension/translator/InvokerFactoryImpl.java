package org.jboss.weld.lite.extension.translator;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import jakarta.enterprise.inject.build.compatible.spi.BeanInfo;
import jakarta.enterprise.inject.build.compatible.spi.InvokerFactory;
import jakarta.enterprise.inject.build.compatible.spi.InvokerInfo;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.enterprise.invoke.InvokerBuilder;
import jakarta.enterprise.lang.model.declarations.MethodInfo;

import org.jboss.weld.bean.ClassBean;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.invokable.InvokerInfoBuilder;
import org.jboss.weld.invokable.TargetMethod;

class InvokerFactoryImpl implements InvokerFactory {
    private final BeanManager beanManager;

    InvokerFactoryImpl(BeanManager beanManager) {
        this.beanManager = beanManager;
    }

    @Override
    public InvokerBuilder<InvokerInfo> createInvoker(BeanInfo bean, MethodInfo method) {
        Bean<?> cdiBean = ((BeanInfoImpl) bean).cdiBean;
        if (!(cdiBean instanceof ClassBean)) {
            throw new DeploymentException("Cannot build invoker for a bean which is not a managed bean: " + cdiBean);
        }
        if (cdiBean instanceof Interceptor) {
            throw new DeploymentException("Cannot build invoker for an interceptor: " + cdiBean);
        }
        if (cdiBean instanceof Decorator) { // not representable in BCE, but can happen
            throw new DeploymentException("Cannot build invoker for a decorator: " + cdiBean);
        }

        if (method.isConstructor()) {
            throw new DeploymentException("Cannot build invoker for a constructor: " + method);
        }
        if (Modifier.isPrivate(method.modifiers())) {
            throw new DeploymentException("Cannot build invoker for a private method: " + method);
        }
        if ("java.lang.Object".equals(method.declaringClass().name())
                && !"toString".equals(method.name())) {
            throw new DeploymentException("Cannot build invoker for a method declared on java.lang.Object: " + method);
        }

        if (method instanceof MethodInfoImpl) {
            // at this point, it is always a Method, not a Constructor
            Method reflectionMethod = (Method) ((MethodInfoImpl) method).reflection;

            // verify that the `methodInfo` belongs to this bean
            if (!ReflectionMembers.allMethods(cdiBean.getBeanClass()).contains(reflectionMethod)) {
                throw new DeploymentException("Method does not belong to bean " + cdiBean.getBeanClass().getName()
                        + ": " + method);
            }

            AnnotatedType<?> cdiBeanClass = ((ClassBean<?>) cdiBean).getAnnotated();
            AnnotatedMethod<?> cdiMethod = (AnnotatedMethod<?>) ((MethodInfoImpl) method).cdiDeclaration;
            TargetMethod targetMethod = cdiMethod != null ? new TargetMethod(cdiMethod) : new TargetMethod(reflectionMethod);
            return new InvokerInfoBuilder<>(cdiBeanClass, targetMethod, beanManager);
        } else {
            throw new DeploymentException("Custom implementations of MethodInfo are not supported!");
        }
    }
}
