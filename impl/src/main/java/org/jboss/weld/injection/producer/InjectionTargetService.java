package org.jboss.weld.injection.producer;

import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.Container;
import org.jboss.weld.ContainerState;
import org.jboss.weld.bootstrap.Validator;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.manager.BeanManagerImpl;

public class InjectionTargetService implements Service {

    private final Validator validator;
    private final Container container;
    private final BeanManagerImpl beanManager;

    private final Collection<InjectionTarget<?>> injectionTargetsToValidate;
    private final Collection<InjectionTargetInitializationContext<?>> injectionTargetsToInitialize;

    public InjectionTargetService(BeanManagerImpl beanManager) {
        this.validator = new Validator();
        this.injectionTargetsToValidate = new ArrayList<InjectionTarget<?>>();
        this.injectionTargetsToInitialize = new ArrayList<InjectionTargetInitializationContext<?>>();
        this.container = Container.instance();
        this.beanManager = beanManager;
    }

    public void addInjectionTargetToBeValidated(InjectionTarget<?> injectionTarget) {
        if (container.getState().equals(ContainerState.VALIDATED)) {
            // Validate now and don't store for later validation as this has been created at runtime
            validator.validateInjectionTarget(injectionTarget, beanManager);
        } else {
            injectionTargetsToValidate.add(injectionTarget);
        }
    }

    public void addInjectionTargetToBeInitialized(InjectionTargetInitializationContext<?> initializationContext) {
        if (container.getState().equals(ContainerState.VALIDATED) || container.getState().equals(ContainerState.INITIALIZED)) {
            // initialize now and don't store for later initialization as this has been created at runtime
            initializationContext.initialize();
        } else {
            injectionTargetsToInitialize.add(initializationContext);
        }
    }

    public void initialize() {
        for (InjectionTargetInitializationContext<?> initializationContext : injectionTargetsToInitialize) {
            initializationContext.initialize();
        }
        injectionTargetsToInitialize.clear();
    }

    public void validate() {
        validator.validateInjectionTargets(injectionTargetsToValidate, beanManager);
        injectionTargetsToValidate.clear();
    }

    public void cleanup() {
    }

}
