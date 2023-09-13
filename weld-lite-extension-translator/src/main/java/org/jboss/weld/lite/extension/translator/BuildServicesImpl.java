package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.build.compatible.spi.AnnotationBuilderFactory;
import jakarta.enterprise.inject.build.compatible.spi.BuildServices;
import jakarta.enterprise.inject.spi.CDI;

import org.jboss.weld.lite.extension.translator.logging.LiteExtensionTranslatorLogger;

public class BuildServicesImpl implements BuildServices {

    // initialized from LiteExtensionTranslator as soon as we have BeanManager reference ready
    static AnnotationBuilderFactoryImpl ANN_FACTORY_IMPL_INSTANCE;

    @Override
    public AnnotationBuilderFactory annotationBuilderFactory() {
        if (ANN_FACTORY_IMPL_INSTANCE != null) {
            return ANN_FACTORY_IMPL_INSTANCE;
        } else {
            // this should never really happen because the factory is used within the extension itself
            // but if it does, we can still fall back to CDI.current() to get BM reference and log a warning
            LiteExtensionTranslatorLogger.LOG.annotationFactoryInstanceNotInitialized();
            return new AnnotationBuilderFactoryImpl(CDI.current().getBeanManager());
        }
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
