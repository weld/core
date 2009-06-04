package javax.enterprise.inject.spi;

import java.util.List;

public interface AnnotatedCallable<X> extends AnnotatedMember<X> {
    public List<AnnotatedParameter<X>> getParameters();
}
