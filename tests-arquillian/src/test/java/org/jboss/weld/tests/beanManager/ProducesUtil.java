package org.jboss.weld.tests.beanManager;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;

@Dependent
public class ProducesUtil {

    @Produces
    @SessionScoped
    @Named("myBean")
    UserInfo getUserInfo() {
        return new UserInfo() {

            private static final long serialVersionUID = 1L;

            public String getUsername() {
                return "pmuir";
            }
        };
    }
}
