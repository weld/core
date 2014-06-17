package org.jboss.weld.environment.servlet.test.injection;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class BatSessionListener extends BatListener implements HttpSessionListener {

    public void sessionCreated(HttpSessionEvent se) {
        if (isSewerNameOk()) {
            se.getSession().setAttribute(BAT_ATTRIBUTE_NAME, Boolean.TRUE);
        }
    }

    public void sessionDestroyed(HttpSessionEvent se) {
        isSewerNameOk();
    }

}
