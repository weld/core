package org.jboss.weld.tests.interceptors.bindingconflict;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@MyBinding(0)
public class MyBean {

    @MyBinding(1)   // NOTE: this is in conflict with class' MyBinding(0)
    public void foo() {
    }
}
