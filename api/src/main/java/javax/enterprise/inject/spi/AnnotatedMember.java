package javax.enterprise.inject.spi;

import java.lang.reflect.Member;

public interface AnnotatedMember<X> extends Annotated {
    public Member getJavaMember();
    public boolean isStatic();
    public AnnotatedType<X> getDeclaringType();
}
