package org.jboss.weld.osgi.examples.userdoc.helloworld.provider.impl;

import org.jboss.weld.osgi.examples.userdoc.helloworld.provider.api.Language;
import org.jboss.weld.osgi.examples.userdoc.helloworld.provider.api.Presentation;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@Presentation
public class PresentationInterceptor {

    @AroundInvoke
    public Object present(InvocationContext ctx) throws Exception {
        ctx.proceed();
        Language language = ctx.getMethod().getDeclaringClass().getAnnotation(Language.class);
        if(language != null) {
            String lang = language.value();
            if(lang != null) {
                if(lang.equals("FRENCH")) {
                    System.out.println("Je suis le bundle hello-world-provider");
                    return null;
                } else if(lang.equals("GERMAN")) {
                    System.out.println("Ich bin das bundle hello-world-provider");
                    return null;
                }
            }
        }
        System.out.println("I am the bundle hello-world-provider");
        return null;
    }
}
