package org.jboss.weld.environment.servlet.test.injection;

import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;

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
