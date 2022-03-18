package org.jboss.weld.lite.extension.translator;

import java.util.List;

class ExtensionPhaseDiscovery extends ExtensionPhaseBase {
    private final jakarta.enterprise.inject.spi.BeforeBeanDiscovery bbd;
    private final List<MetaAnnotationsImpl.StereotypeConfigurator<?>> stereotypes;
    private final List<MetaAnnotationsImpl.ContextData> contexts;
    private final ClassLoader cl;

    ExtensionPhaseDiscovery(jakarta.enterprise.inject.spi.BeanManager beanManager, ExtensionInvoker util,
            SharedErrors errors, jakarta.enterprise.inject.spi.BeforeBeanDiscovery bbd,
            List<MetaAnnotationsImpl.StereotypeConfigurator<?>> stereotypes,
            List<MetaAnnotationsImpl.ContextData> contexts, ClassLoader cl) {
        super(ExtensionPhase.DISCOVERY, beanManager, util, errors);
        this.bbd = bbd;
        this.stereotypes = stereotypes;
        this.contexts = contexts;
        this.cl = cl;
    }

    @Override
    Object argumentForExtensionMethod(ExtensionMethodParameterType type, java.lang.reflect.Method method) {
        switch (type) {
            case META_ANNOTATIONS:
                return new MetaAnnotationsImpl(bbd, stereotypes, contexts, beanManager);
            case SCANNED_CLASSES:
                return new ScannedClassesImpl(bbd, cl);

            default:
                return super.argumentForExtensionMethod(type, method);
        }
    }
}
