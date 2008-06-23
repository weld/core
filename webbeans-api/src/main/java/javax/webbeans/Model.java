package javax.webbeans;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Named
@RequestScoped
@Production
@Stereotype
@Target( { TYPE, METHOD })
@Retention(RUNTIME)
@Documented
public @interface Model
{

}
