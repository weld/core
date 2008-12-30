package javax.webbeans;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Set;

import javax.webbeans.manager.Bean;

public interface InjectionPoint { 
	public Type getType();
	public Set<Annotation> getBindingTypes();
	public Object getInstance();
	public Bean<?> getBean();
	public Member getMember();
	public <T extends Annotation> T getAnnotation(Class<T> annotationType);
	public Annotation[] getAnnotations();
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationType);
} 
