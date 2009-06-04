package javax.enterprise.inject.spi;

import java.lang.annotation.Annotation;

public interface BeforeBeanDiscovery {
    public void addBindingType(Class<? extends Annotation> bindingType);
    public void addScopeType(Class<? extends Annotation> scopeType, boolean normal, boolean passivating);
    public void addStereotype(Class<? extends Annotation> stereotype, Annotation... stereotypeDef);
    public void addInterceptorBindingType(Class<? extends Annotation> bindingType);
}
