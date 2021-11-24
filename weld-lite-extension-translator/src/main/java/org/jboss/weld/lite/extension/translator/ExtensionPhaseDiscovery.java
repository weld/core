package org.jboss.weld.lite.extension.translator;

import java.util.List;

class ExtensionPhaseDiscovery extends ExtensionPhaseBase {
    private final jakarta.enterprise.inject.spi.BeforeBeanDiscovery bbd;
    private final List<MetaAnnotationsImpl.StereotypeConfigurator<?>> stereotypes;
    private final List<MetaAnnotationsImpl.ContextData> contexts;

    ExtensionPhaseDiscovery(jakarta.enterprise.inject.spi.BeanManager beanManager, ExtensionInvoker util,
            SharedErrors errors, jakarta.enterprise.inject.spi.BeforeBeanDiscovery bbd,
            List<MetaAnnotationsImpl.StereotypeConfigurator<?>> stereotypes,
            List<MetaAnnotationsImpl.ContextData> contexts) {
        super(ExtensionPhase.DISCOVERY, beanManager, util, errors);
        this.bbd = bbd;
        this.stereotypes = stereotypes;
        this.contexts = contexts;
    }

    @Override
    Object argumentForExtensionMethod(ExtensionMethodParameterType type, java.lang.reflect.Method method) {
        switch (type) {
            case META_ANNOTATIONS:
                return new MetaAnnotationsImpl(bbd, stereotypes, contexts);
            case SCANNED_CLASSES:
                return new ScannedClassesImpl(bbd);

            default:
                return super.argumentForExtensionMethod(type, method);
        }
    }
}
