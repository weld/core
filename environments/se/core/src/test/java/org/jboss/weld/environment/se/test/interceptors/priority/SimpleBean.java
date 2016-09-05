package org.jboss.weld.environment.se.test.interceptors.priority;

@SimpleInterceptorBinding
public class SimpleBean {

    public String simpleMethod() {
        return "implemented";
    }

}
