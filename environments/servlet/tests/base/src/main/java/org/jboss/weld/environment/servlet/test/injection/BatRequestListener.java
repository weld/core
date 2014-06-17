package org.jboss.weld.environment.servlet.test.injection;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

public class BatRequestListener extends BatListener implements ServletRequestListener {

    public void requestInitialized(ServletRequestEvent sre) {
        if (isSewerNameOk()) {
            sre.getServletRequest().setAttribute(BAT_ATTRIBUTE_NAME, Boolean.TRUE);
        }
    }

    public void requestDestroyed(ServletRequestEvent sre) {
        isSewerNameOk();
    }

}
