package javax.webbeans;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Named
@RequestScoped
@Stereotype
@Target( { TYPE, METHOD })
@Retention(RUNTIME)
/**
 * A stereotype for MVC model objects
 * 
 * @author Gavin King
 */
public @interface Model {
}