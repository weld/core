package org.jboss.weld.tests.smoke;


/**
 * @author Sam Corbet
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class Crasher {
    protected class NonStaticInner {
        public NonStaticInner(Integer a) {
            System.out.println("Hi" + a);
        }

        protected class NonStaticInner2 {
            public NonStaticInner2(Double a) {
                System.out.println("Hi" + a);
            }
        }
    }

    public enum Enum1 {
    }

    public enum Enum2 {
        FOO(1);

        @SuppressWarnings("unused")
        private final int foo;

        private Enum2(int foo) {
            this.foo = foo;
        }
    }
}
