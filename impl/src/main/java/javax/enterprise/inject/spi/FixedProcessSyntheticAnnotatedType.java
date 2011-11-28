package javax.enterprise.inject.spi;

/**
 * <p>
 * The container fires an event of this type for each Java class or interface added by
 * {@link BeforeBeanDiscovery#addAnnotatedType(AnnotatedType)}.
 * </p>
 * <p>
 * Any observer of this event is permitted to wrap and/or replace the
 * {@link javax.enterprise.inject.spi.AnnotatedType}. The container must use the final value of this
 * property, after all observers have been called, to discover the types and read the annotations of
 * the program elements.
 * </p>
 * <p>
 * For example, the following observer decorates the
 * {@link javax.enterprise.inject.spi.AnnotatedType} for every class that is added by
 * {@link BeforeBeanDiscovery#addAnnotatedType(AnnotatedType)}.
 * </p>
 *
 * <pre>
 * public &lt;T&gt; void decorateAnnotatedType(@Observes ProcessSyntheticAnnotatedType&lt;T&gt; pat) {
 *    pat.setAnnotatedType(decorate(pat.getAnnotatedType()));
 * }
 * </pre>
 * <p>
 * If any observer method of a {@code ProcessSyntheticAnnotatedType} event throws an exception, the
 * exception is treated as a definition error by the container.
 * </p>
 *
 * @author David Allen
 * @author Pete Muir
 * @see AnnotatedType
 * @see ProcessAnnotatedType
 * @param <X> The class being annotated
 */
// TODO: remove once CDI API defines this properly
public interface FixedProcessSyntheticAnnotatedType<X> extends ProcessAnnotatedType<X> {
   /**
    * Get the extension instance which added the {@link AnnotatedType} for which this event is being
    * fired.
    *
    * @return the extension instance
    */
   Extension getSource();
}