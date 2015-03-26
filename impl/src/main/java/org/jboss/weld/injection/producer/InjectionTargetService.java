package org.jboss.weld.injection.producer;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Producer;

import org.jboss.weld.Container;
import org.jboss.weld.ContainerState;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bootstrap.Validator;
import org.jboss.weld.bootstrap.api.helpers.AbstractBootstrapService;
import org.jboss.weld.manager.BeanManagerImpl;

public class InjectionTargetService extends AbstractBootstrapService {

    private final Validator validator;
    private final Container container;
    private final BeanManagerImpl beanManager;

    private final Collection<Producer<?>> producersToValidate;
    private final Collection<InjectionTargetInitializationContext<?>> injectionTargetsToInitialize;

    public InjectionTargetService(BeanManagerImpl beanManager) {
        this.validator = beanManager.getServices().get(Validator.class);
        this.producersToValidate = new ConcurrentLinkedQueue<Producer<?>>();
        this.injectionTargetsToInitialize = new ConcurrentLinkedQueue<InjectionTargetInitializationContext<?>>();
        this.container = Container.instance(beanManager);
        this.beanManager = beanManager;
    }

    public void validateProducer(Producer<?> producer) {
        if (container.getState().equals(ContainerState.VALIDATED) || container.getState().equals(ContainerState.INITIALIZED)) {
            // We are past the bootstrap and therefore we can validate the producer immediately
            validator.validateProducer(producer, beanManager);
        } else {
            // Validate injection points for definition errors now
            for (InjectionPoint ip : producer.getInjectionPoints()) {
                validator.validateInjectionPointForDefinitionErrors(ip, ip.getBean(), beanManager);
                validator.validateEventMetadataInjectionPoint(ip);
            }
            // Schedule validation for deployment problems to be done later
            producersToValidate.add(producer);
        }
    }

    public <T> void addInjectionTargetToBeInitialized(EnhancedAnnotatedType<T> type, BasicInjectionTarget<T> injectionTarget) {
        addInjectionTargetToBeInitialized(new InjectionTargetInitializationContext<T>(type, injectionTarget));
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
        validator.validateProducers(producersToValidate, beanManager);
        producersToValidate.clear();
    }

    @Override
    public void cleanupAfterBoot() {
        producersToValidate.clear();
        injectionTargetsToInitialize.clear();
    }

}
