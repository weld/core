package org.jboss.weld.lite.extension.translator;

import java.util.List;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;

class ExtensionPhaseSynthesis extends ExtensionPhaseBase {
    private final List<SyntheticBeanBuilderImpl<?>> syntheticBeans;
    private final List<SyntheticObserverBuilderImpl<?>> syntheticObservers;

    ExtensionPhaseSynthesis(jakarta.enterprise.inject.spi.BeanManager beanManager, ExtensionInvoker util,
            SharedErrors errors, List<SyntheticBeanBuilderImpl<?>> syntheticBeans,
            List<SyntheticObserverBuilderImpl<?>> syntheticObservers) {
        super(ExtensionPhase.SYNTHESIS, beanManager, util, errors);
        this.syntheticBeans = syntheticBeans;
        this.syntheticObservers = syntheticObservers;
    }

    @Override
    Object argumentForExtensionMethod(ExtensionMethodParameterType type, java.lang.reflect.Method method) {
        switch (type) {
            case SYNTHETIC_COMPONENTS:
                return new SyntheticComponentsImpl(syntheticBeans, syntheticObservers,
                        (Class<? extends BuildCompatibleExtension>) method.getDeclaringClass());
            case TYPES:
                return new TypesImpl(beanManager);

            default:
                return super.argumentForExtensionMethod(type, method);
        }
    }
}
