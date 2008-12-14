package org.jboss.webbeans.test.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import javax.webbeans.BindingType;

@BindingType
@Retention(RUNTIME)
public @interface Role
{
   String value();
}
