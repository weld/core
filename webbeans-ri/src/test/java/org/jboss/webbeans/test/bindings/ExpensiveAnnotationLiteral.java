package org.jboss.webbeans.test.bindings;

import javax.webbeans.AnnotationLiteral;

import org.jboss.webbeans.test.annotations.Expensive;

public abstract class ExpensiveAnnotationLiteral extends AnnotationLiteral<Expensive> implements Expensive
{
   
}
