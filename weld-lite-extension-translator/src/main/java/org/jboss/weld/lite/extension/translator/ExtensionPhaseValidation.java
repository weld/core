package org.jboss.weld.lite.extension.translator;

import org.jboss.weld.manager.api.WeldManager;

class ExtensionPhaseValidation extends ExtensionPhaseBase {
    ExtensionPhaseValidation(jakarta.enterprise.inject.spi.BeanManager beanManager, ExtensionInvoker util,
            SharedErrors errors) {
        super(ExtensionPhase.VALIDATION, beanManager, util, errors);
    }

    @Override
    Object argumentForExtensionMethod(ExtensionMethodParameterType type, java.lang.reflect.Method method) {
        if (type == ExtensionMethodParameterType.TYPES) {
            return new TypesImpl(beanManager);
        }
        if (type == ExtensionMethodParameterType.INVOKER_VALIDATION) {
            return new InvokerValidationImpl((WeldManager) beanManager, errors);
        }

        return super.argumentForExtensionMethod(type, method);
    }
}
