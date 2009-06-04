package javax.enterprise.inject.spi;

public interface AnnotatedParameter<X> extends Annotated {
    public int getPosition();
    public AnnotatedCallable<X> getDeclaringCallable();
}
