package org.jboss.weld.lite.extension.translator;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.NormalScope;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.AmbiguousResolutionException;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanDisposer;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticInjections;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticObserver;
import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.bootstrap.events.AfterBeanDiscoveryImpl;
import org.jboss.weld.lite.extension.translator.logging.LiteExtensionTranslatorLogger;

/**
 * This CDI extension allows execution of build compatible extensions (BCE) via portable extensions (PE)
 * by mapping phases of BCE onto PE.
 *
 * <p>
 * This extension is by default disabled and integrators need to manually register it with Weld container when
 * bootstrapping it. For SE and servlet, this is done directly in Weld. However, for EE integrators need to determine
 * the root deployment and register the extension themselves.
 * </p>
 */
public class LiteExtensionTranslator implements jakarta.enterprise.inject.spi.Extension {
    private final ExtensionInvoker util;
    private final ClassLoader cl; // class loader to be used, WFLY for instance needs to set its own
    private final SharedErrors errors = new SharedErrors();

    private final List<Class<? extends jakarta.enterprise.context.spi.AlterableContext>> contextsToRegister = new ArrayList<>();

    private final List<ExtensionPhaseEnhancementAction> enhancementActions = new ArrayList<>();
    private final List<ExtensionPhaseRegistrationAction> registrationActions = new ArrayList<>();
    private final List<SyntheticBeanBuilderImpl.InjectionPointDeclaration> syntheticInjectionPoints = new ArrayList<>();

    private jakarta.enterprise.inject.spi.BeanManager bm;

    public LiteExtensionTranslator() {
        this(BuildCompatibleExtensionLoader.getBuildCompatibleExtensions(), Thread.currentThread().getContextClassLoader());
    }

    /**
     * Deprecated, use {@link LiteExtensionTranslator#LiteExtensionTranslator(Collection, ClassLoader)}.
     * This method will be removed in future versions.
     */
    @Deprecated(forRemoval = true)
    public LiteExtensionTranslator(List<Class<? extends BuildCompatibleExtension>> buildCompatibleExtensions, ClassLoader cl) {
        this((Collection<Class<? extends BuildCompatibleExtension>>) buildCompatibleExtensions, cl);
    }

    public LiteExtensionTranslator(Collection<Class<? extends BuildCompatibleExtension>> buildCompatibleExtensions,
            ClassLoader cl) {
        this.util = new ExtensionInvoker(buildCompatibleExtensions);
        this.cl = cl;
        // clear out information about extensions we found, this is to prevent issues in test environments where this
        // could interfere when subsequent tests are run on the same JVM
        BuildCompatibleExtensionLoader.clearDiscoveredExtensions();
    }

    public void discovery(@Priority(Integer.MAX_VALUE) @Observes jakarta.enterprise.inject.spi.BeforeBeanDiscovery bbd,
            jakarta.enterprise.inject.spi.BeanManager bm) {

        this.bm = bm;
        // initialize annotation factory instance
        BuildServicesImpl.ANN_FACTORY_IMPL_INSTANCE = new AnnotationBuilderFactoryImpl(bm);

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
                    scopeAnnotation = context.contextClass.getConstructor().newInstance().getScope();
                } catch (InvocationTargetException e) {
                    throw LiteExtensionTranslatorLogger.LOG.unableToInstantiateObject(context.contextClass,
                            e.getCause().toString(), e);
                } catch (ReflectiveOperationException e) {
                    throw LiteExtensionTranslatorLogger.LOG.unableToInstantiateObject(context.contextClass, e.toString(), e);
                }
            }

            boolean isNormal;
            boolean isPassivating;
            if (context.isNormal != null) {
                isNormal = context.isNormal;
                // in case the scope was declared normal via boolean, we assume it cannot be passivating
                isPassivating = false;
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
    }

    public void enhancement(@Priority(Integer.MAX_VALUE) @Observes jakarta.enterprise.inject.spi.ProcessAnnotatedType<?> pat) {
        for (ExtensionPhaseEnhancementAction enhancementAction : enhancementActions) {
            enhancementAction.run(pat);
        }
    }

    public void registration(@Priority(Integer.MAX_VALUE) @Observes jakarta.enterprise.inject.spi.AfterTypeDiscovery atd) {
        new ExtensionPhaseRegistration(bm, util, errors, registrationActions).run();
    }

    public void collectBeans(@Priority(Integer.MAX_VALUE) @Observes jakarta.enterprise.inject.spi.ProcessBean<?> pb) {
        // for synthetic beans, this will run @Registration before @Synthesis is fully over, maybe change that?
        for (ExtensionPhaseRegistrationAction registrationAction : registrationActions) {
            registrationAction.run(pb);
        }
    }

    public void collectObservers(
            @Priority(Integer.MAX_VALUE) @Observes jakarta.enterprise.inject.spi.ProcessObserverMethod<?, ?> pom) {
        // for synthetic observers, this will run @Registration before @Synthesis is fully over, maybe change that?
        for (ExtensionPhaseRegistrationAction registrationAction : registrationActions) {
            registrationAction.run(pom);
        }
    }

    public void synthesis(@Priority(Integer.MAX_VALUE) @Observes jakarta.enterprise.inject.spi.AfterBeanDiscovery abd) {

        for (Class<? extends jakarta.enterprise.context.spi.AlterableContext> contextClass : contextsToRegister) {
            try {
                abd.addContext(contextClass.getConstructor().newInstance());
            } catch (InvocationTargetException e) {
                throw LiteExtensionTranslatorLogger.LOG.unableToInstantiateObject(contextClass, e.getCause().toString(), e);
            } catch (ReflectiveOperationException e) {
                throw LiteExtensionTranslatorLogger.LOG.unableToInstantiateObject(contextClass, e.toString(), e);
            }
        }

        List<SyntheticBeanBuilderImpl<?>> syntheticBeans = new ArrayList<>();
        List<SyntheticObserverBuilderImpl<?>> syntheticObservers = new ArrayList<>();

        new ExtensionPhaseSynthesis(bm, util, errors, syntheticBeans, syntheticObservers).run();

        for (SyntheticBeanBuilderImpl<?> syntheticBean : syntheticBeans) {
            jakarta.enterprise.inject.spi.configurator.BeanConfigurator<Object> configurator;
            if (abd instanceof AfterBeanDiscoveryImpl) {
                // specify the receiver class to be the BCE extension, linking the bean to it
                // in EE env this affects the BM used for dynamic resolution inside bean creation method
                configurator = ((AfterBeanDiscoveryImpl) abd).addBean(syntheticBean.extensionClass,
                        LiteExtensionTranslator.class);
            } else {
                configurator = abd.addBean();
            }
            configurator.beanClass(syntheticBean.implementationClass);
            configurator.types(syntheticBean.types);
            configurator.qualifiers(syntheticBean.qualifiers);
            if (syntheticBean.scope != null) {
                configurator.scope(syntheticBean.scope);
            }
            configurator.alternative(syntheticBean.isAlternative);
            configurator.reserve(syntheticBean.isReserve);
            configurator.eager(syntheticBean.isEager);
            configurator.priority(syntheticBean.priority);
            configurator.name(syntheticBean.name);
            configurator.stereotypes(syntheticBean.stereotypes);
            boolean creatorUsesNewApi = usesNewCreateApi(syntheticBean.creatorClass);
            configurator.produceWith(lookup -> {
                try {
                    SyntheticBeanCreator creator = syntheticBean.creatorClass.getConstructor().newInstance();
                    if (creatorUsesNewApi) {
                        return creator.create(
                                new SyntheticInjectionsImpl(lookup, syntheticBean.injectionPoints),
                                new ParametersImpl(syntheticBean.params));
                    } else {
                        return creator.create(lookup, new ParametersImpl(syntheticBean.params));
                    }
                } catch (InvocationTargetException e) {
                    throw LiteExtensionTranslatorLogger.LOG.unableToInstantiateObject(syntheticBean.creatorClass,
                            e.getCause().toString(), e);
                } catch (ReflectiveOperationException e) {
                    throw LiteExtensionTranslatorLogger.LOG.unableToInstantiateObject(syntheticBean.creatorClass, e.toString(),
                            e);
                }
            });
            if (syntheticBean.disposerClass != null) {
                boolean disposerUsesNewApi = usesNewDisposeApi(syntheticBean.disposerClass);
                configurator.disposeWith((object, lookup) -> {
                    try {
                        SyntheticBeanDisposer disposer = syntheticBean.disposerClass.getConstructor()
                                .newInstance();
                        if (disposerUsesNewApi) {
                            disposer.dispose(object,
                                    new SyntheticInjectionsImpl(lookup, syntheticBean.injectionPoints),
                                    new ParametersImpl(syntheticBean.params));
                        } else {
                            disposer.dispose(object, lookup, new ParametersImpl(syntheticBean.params));
                        }
                    } catch (InvocationTargetException e) {
                        throw LiteExtensionTranslatorLogger.LOG.unableToInstantiateObject(syntheticBean.disposerClass,
                                e.getCause().toString(), e);
                    } catch (ReflectiveOperationException e) {
                        throw LiteExtensionTranslatorLogger.LOG.unableToInstantiateObject(syntheticBean.disposerClass,
                                e.toString(), e);
                    }
                });
            }
            // Collect injection points for validation during @Validation phase
            syntheticInjectionPoints.addAll(syntheticBean.injectionPoints);
        }

        for (SyntheticObserverBuilderImpl<?> syntheticObserver : syntheticObservers) {
            jakarta.enterprise.inject.spi.configurator.ObserverMethodConfigurator<Object> configurator = abd
                    .addObserverMethod();
            configurator.beanClass(syntheticObserver.declaringClass);
            configurator.observedType(syntheticObserver.eventType);
            configurator.qualifiers(syntheticObserver.qualifiers);
            configurator.priority(syntheticObserver.priority);
            configurator.async(syntheticObserver.isAsync);
            configurator.reception(syntheticObserver.reception);
            configurator.transactionPhase(syntheticObserver.transactionPhase);
            configurator.notifyWith(eventContext -> {
                try {
                    SyntheticObserver observer = syntheticObserver.observerClass.getConstructor().newInstance();
                    observer.observe(eventContext, new ParametersImpl(syntheticObserver.params));
                } catch (InvocationTargetException e) {
                    throw LiteExtensionTranslatorLogger.LOG.unableToInstantiateObject(syntheticObserver.observerClass,
                            e.getCause().toString(), e);
                } catch (ReflectiveOperationException e) {
                    throw LiteExtensionTranslatorLogger.LOG.unableToInstantiateObject(syntheticObserver.observerClass,
                            e.toString(), e);
                }
            });
        }
    }

    public void validation(@Priority(Integer.MAX_VALUE) @Observes jakarta.enterprise.inject.spi.AfterDeploymentValidation adv) {

        try {

            new ExtensionPhaseValidation(bm, util, errors).run();

            // Validate synthetic bean injection points
            for (SyntheticBeanBuilderImpl.InjectionPointDeclaration ip : syntheticInjectionPoints) {
                Set<Annotation> qualifiers = ip.qualifiers.isEmpty()
                        ? Set.of(Default.Literal.INSTANCE)
                        : ip.qualifiers;
                Set<Bean<?>> beans = bm.getBeans(ip.type, qualifiers.toArray(new Annotation[0]));
                if (beans.isEmpty()) {
                    errors.list.add(LiteExtensionTranslatorLogger.LOG
                            .unsatisfiedSyntheticInjectionPoint(ip.type, qualifiers));
                } else {
                    try {
                        bm.resolve(beans);
                    } catch (AmbiguousResolutionException e) {
                        errors.list.add(LiteExtensionTranslatorLogger.LOG
                                .ambiguousSyntheticInjectionPoint(ip.type, qualifiers));
                    }
                }
            }

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
            syntheticInjectionPoints.clear();

            ReflectionMembers.clearCaches();

            this.bm = null;
        }
    }

    /**
     * Checks whether the creator class declares the new
     * {@code create(SyntheticInjections, Parameters)} method directly.
     * If both new and old methods are declared, throws DeploymentException.
     */
    private static boolean usesNewCreateApi(Class<? extends SyntheticBeanCreator> creatorClass) {
        boolean hasNew = declaresMethod(creatorClass, "create",
                SyntheticInjections.class, Parameters.class);
        boolean hasOld = declaresMethod(creatorClass, "create",
                Instance.class, Parameters.class);
        if (hasNew && hasOld) {
            throw LiteExtensionTranslatorLogger.LOG.syntheticBeanCreatorBothMethods(creatorClass);
        }
        return hasNew;
    }

    /**
     * Checks whether the disposer class declares the new
     * {@code dispose(T, SyntheticInjections, Parameters)} method directly.
     * If both new and old methods are declared, throws DeploymentException.
     */
    private static boolean usesNewDisposeApi(Class<? extends SyntheticBeanDisposer> disposerClass) {
        boolean hasNew = declaresMethod(disposerClass, "dispose",
                Object.class, SyntheticInjections.class, Parameters.class);
        boolean hasOld = declaresMethod(disposerClass, "dispose",
                Object.class, Instance.class, Parameters.class);
        if (hasNew && hasOld) {
            throw LiteExtensionTranslatorLogger.LOG.syntheticBeanDisposerBothMethods(disposerClass);
        }
        return hasNew;
    }

    /**
     * Checks if a method with the given name and parameter types is declared
     * directly on the class (not inherited from a superclass or interface).
     */
    private static boolean declaresMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        try {
            clazz.getDeclaredMethod(name, paramTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
