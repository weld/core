package org.jboss.weld.module.web.el;

class CreationalContextCallable {

    private ELCreationalContext<?> ctx;

    ELCreationalContext<?> get() {
        if (ctx == null) {
            ctx = new ELCreationalContext<Object>(null);
        }
        return ctx;
    }

    boolean exists() {
        return ctx != null;
    }

}
