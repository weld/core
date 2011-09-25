package org.jboss.weld.el;

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
