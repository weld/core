package org.jboss.weld.tests.beanManager;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

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
