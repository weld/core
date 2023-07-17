package org.jboss.weld.literal;

import jakarta.enterprise.invoke.Invokable;
import jakarta.enterprise.util.AnnotationLiteral;

/**
 * Annotation literal for Invokable
 */
// TODO should this be in CDI instead?
public class InvokableLiteral extends AnnotationLiteral<Invokable> implements Invokable {

    private static final long serialVersionUID = 7312029852030451655L;

    public static final Invokable INSTANCE = new InvokableLiteral();

    private InvokableLiteral() {
    }
}
