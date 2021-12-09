package org.jboss.weld.lite.extension.translator;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.NormalScope;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanDisposer;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticObserver;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This CDI extension allows execution of build compatible extensions (BCE) via portable extensions (PE)
 * by mapping phases of BCE onto PE.
 *
 * <p>
 *  This extension is by default disabled and integrators need to manually register it with Weld container when
 *  bootstrapping it. For SE and servlet, this is done directly in Weld. However, for EE integrators need to determine
 *  the root deployment and register the extension themselves.
 * </p>
 */
public class LiteExtensionTranslator implements jakarta.enterprise.inject.spi.Extension {
    private final ExtensionInvoker util;
    private final ClassLoader cl; // class loader to be used, WFLY for instance needs to set its own
    private final SharedErrors errors = new SharedErrors();

    private final List<Class<? extends jakarta.enterprise.context.spi.AlterableContext>> contextsToRegister = new ArrayList<>();

    private final List<ExtensionPhaseEnhancementAction> enhancementActions = new ArrayList<>();
    private final List<ExtensionPhaseRegistrationAction> registrationActions = new ArrayList<>();

    public LiteExtensionTranslator() {
        this.util = new ExtensionInvoker();
        this.cl = Thread.currentThread().getContextClassLoader();
    }

    public LiteExtensionTranslator(Collection<Class<? extends BuildCompatibleExtension>> buildCompatibleExtensions, ClassLoader cl) {
        this.util = new ExtensionInvoker(buildCompatibleExtensions);
        this.cl = cl;
    }

    public void discovery(@Priority(Integer.MAX_VALUE) @Observes jakarta.enterprise.inject.spi.BeforeBeanDiscovery bbd,
            jakarta.enterprise.inject.spi.BeanManager bm) {

        try {
            BeanManagerAccess.set(bm);

            List<MetaAnnotationsImpl.StereotypeConfigurator<?>> stereotypes = new ArrayList<>();
            List<MetaAnnotationsImpl.ContextData> contexts = new ArrayList<>();

            new ExtensionPhaseDiscovery(bm, util, errors, bbd, stereotypes, contexts, cl).run();

            // qualifiers and interceptor bindings are handled directly in MetaAnnotationsImpl (via BBD)
            for (MetaAnnotationsImpl.StereotypeConfigurator<?> stereotype : stereotypes) {
                bbd.addStereotype(stereotype.annotation, stereotype.annotations.toArray(new Annotation[0]));
            }

            for (MetaAnnotationsImpl.ContextData context : contexts) {
                Class<? extends Annotation> scopeAnnotation = context.scopeAnnotation;
                if (scopeAnnotation == null) {
                    try {
                        scopeAnnotation = context.contextClass.newInstance().getScope();
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                }

                boolean isNormal;
                boolean isPassivating;
                if (context.isNormal != null) {
                    isNormal = context.isNormal;
                    isPassivating = false; // TODO
                } else {
                    NormalScope normalScope = scopeAnnotation.getAnnotation(NormalScope.class);
                    if (normalScope != null) {
                        isNormal = true;
                        isPassivating = normalScope.passivating();
                    } else {
                        isNormal = false;
                        isPassivating = false;
                    }
                }

                bbd.addScope(scopeAnnotation, isNormal, isPassivating);

                Class<? extends jakarta.enterprise.context.spi.AlterableContext> contextClass = context.contextClass;
                contextsToRegister.add(contextClass);
            }

            new ExtensionPhaseEnhancement(bm, util, errors, enhancementActions).run();
        } finally {
            BeanManagerAccess.remove();
        }
    }

    public void enhancement(@Priority(Integer.MAX_VALUE) @Observes jakarta.enterprise.inject.spi.ProcessAnnotatedType<?> pat,
            jakarta.enterprise.inject.spi.BeanManager bm) {

        try {
            BeanManagerAccess.set(bm);

            for (ExtensionPhaseEnhancementAction enhancementAction : enhancementActions) {
                enhancementAction.run(pat);
            }
        } finally {
            BeanManagerAccess.remove();
        }
    }

    public void registration(@Priority(Integer.MAX_VALUE) @Observes jakarta.enterprise.inject.spi.AfterTypeDiscovery atd,
            jakarta.enterprise.inject.spi.BeanManager bm) {

        try {
            BeanManagerAccess.set(bm);

            new ExtensionPhaseRegistration(bm, util, errors, registrationActions).run();
        } finally {
            BeanManagerAccess.remove();
        }
    }

    public void collectBeans(@Priority(Integer.MAX_VALUE) @Observes jakarta.enterprise.inject.spi.ProcessBean<?> pb,
            jakarta.enterprise.inject.spi.BeanManager bm) {

        try {
            BeanManagerAccess.set(bm);

            // for synthetic beans, this will run @Registration before @Synthesis is fully over, maybe change that?
            for (ExtensionPhaseRegistrationAction registrationAction : registrationActions) {
                registrationAction.run(pb);
            }
        } finally {
            BeanManagerAccess.remove();
        }
    }

    public void collectObservers(@Priority(Integer.MAX_VALUE) @Observes jakarta.enterprise.inject.spi.ProcessObserverMethod<?, ?> pom,
            jakarta.enterprise.inject.spi.BeanManager bm) {

        try {
            BeanManagerAccess.set(bm);

            // for synthetic observers, this will run @Registration before @Synthesis is fully over, maybe change that?
            for (ExtensionPhaseRegistrationAction registrationAction : registrationActions) {
                registrationAction.run(pom);
            }
        } finally {
            BeanManagerAccess.remove();
        }
    }

    public void synthesis(@Priority(Integer.MAX_VALUE) @Observes jakarta.enterprise.inject.spi.AfterBeanDiscovery abd,
            jakarta.enterprise.inject.spi.BeanManager bm) throws IllegalAccessException, InstantiationException {

        try {
            BeanManagerAccess.set(bm);

            for (Class<? extends jakarta.enterprise.context.spi.AlterableContext> contextClass : contextsToRegister) {
                abd.addContext(contextClass.newInstance());
            }

            List<SyntheticBeanBuilderImpl<?>> syntheticBeans = new ArrayList<>();
            List<SyntheticObserverBuilderImpl<?>> syntheticObservers = new ArrayList<>();

            new ExtensionPhaseSynthesis(bm, util, errors, syntheticBeans, syntheticObservers).run();

            for (SyntheticBeanBuilderImpl<?> syntheticBean : syntheticBeans) {
                jakarta.enterprise.inject.spi.configurator.BeanConfigurator<Object> configurator = abd.addBean();
                configurator.beanClass(syntheticBean.implementationClass);
                configurator.types(syntheticBean.types);
                configurator.qualifiers(syntheticBean.qualifiers);
                if (syntheticBean.scope != null) {
                    configurator.scope(syntheticBean.scope);
                }
                configurator.alternative(syntheticBean.isAlternative);
                configurator.priority(syntheticBean.priority);
                configurator.name(syntheticBean.name);
                configurator.stereotypes(syntheticBean.stereotypes);
                configurator.produceWith(lookup -> {
                    try {
                        SyntheticBeanCreator creator = syntheticBean.creatorClass.newInstance();
                        return creator.create(lookup, new ParametersImpl(syntheticBean.params));
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                });
                if (syntheticBean.disposerClass != null) {
                    configurator.disposeWith((object, lookup) -> {
                        try {
                            SyntheticBeanDisposer disposer = syntheticBean.disposerClass.newInstance();
                            disposer.dispose(object, lookup, new ParametersImpl(syntheticBean.params));
                        } catch (ReflectiveOperationException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }

            for (SyntheticObserverBuilderImpl<?> syntheticObserver : syntheticObservers) {
                jakarta.enterprise.inject.spi.configurator.ObserverMethodConfigurator<Object> configurator = abd.addObserverMethod();
                configurator.beanClass(syntheticObserver.declaringClass);
                configurator.observedType(syntheticObserver.eventType);
                configurator.qualifiers(syntheticObserver.qualifiers);
                configurator.priority(syntheticObserver.priority);
                configurator.async(syntheticObserver.isAsync);
                configurator.reception(syntheticObserver.reception);
                configurator.transactionPhase(syntheticObserver.transactionPhase);
                configurator.notifyWith(eventContext -> {
                    SyntheticObserver observer = syntheticObserver.observerClass.newInstance();
                    observer.observe(eventContext, new ParametersImpl(syntheticObserver.params));
                });
            }
        } finally {
            BeanManagerAccess.remove();
        }
    }

    public void validation(@Priority(Integer.MAX_VALUE) @Observes jakarta.enterprise.inject.spi.AfterDeploymentValidation adv,
            jakarta.enterprise.inject.spi.BeanManager bm) {

        try {
            BeanManagerAccess.set(bm);

            new ExtensionPhaseValidation(bm, util, errors).run();

            for (Throwable error : errors.list) {
                adv.addDeploymentProblem(error);
            }
        } finally {
            // cleanup
            util.clear();
            errors.list.clear();

            contextsToRegister.clear();
            enhancementActions.clear();
            registrationActions.clear();

            BeanManagerAccess.remove();
        }
    }
}
