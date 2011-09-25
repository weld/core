package org.jboss.weld.manager;

import org.jboss.weld.Container;
import org.jboss.weld.ContainerState;
import org.jboss.weld.bootstrap.Validator;
import org.jboss.weld.bootstrap.api.Service;

import javax.enterprise.inject.spi.InjectionTarget;
import java.util.ArrayList;
import java.util.Collection;

public class InjectionTargetValidator implements Service {

    private final Validator validator;
    private final Collection<InjectionTarget<?>> injectionTargets;
    private final Container container;
    private final BeanManagerImpl beanManager;

    public InjectionTargetValidator(BeanManagerImpl beanManager) {
        this.validator = new Validator();
        this.injectionTargets = new ArrayList<InjectionTarget<?>>();
        this.container = Container.instance(beanManager.getContextId());
        this.beanManager = beanManager;
    }

    public void addInjectionTarget(InjectionTarget<?> injectionTarget) {
        if (container.getState().equals(ContainerState.VALIDATED)) {
            // Validate now and don't store for later validation as this has been created at runtime
            validator.validateInjectionTarget(injectionTarget, beanManager);
        } else {
            injectionTargets.add(injectionTarget);
        }
    }

    public void validate() {
        for (InjectionTarget<?> injectionTarget : injectionTargets) {
            validator.validateInjectionTarget(injectionTarget, beanManager);
        }
        injectionTargets.clear();
    }

    public void cleanup() {

    }

}
