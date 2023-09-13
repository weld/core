package org.jboss.weld.tests.unit.selector;

import static org.jboss.weld.metadata.Selectors.matchPath;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jboss.weld.tests.unit.Qux;
import org.jboss.weld.tests.unit.selector.subpackage.Baz;
import org.junit.Test;

public class SelectorTest {

    private static final String THIS_PACKAGE = SelectorTest.class.getPackage().getName();

    @Test
    public void testSelector() {
        // Test whether a class name matches the same class name
        assertTrue(matchPath(Foo.class.getName(), Foo.class.getName()));

        // ---- Test non-deep matches ----

        //Test whether classes in the package match
        assertTrue(matchPath(THIS_PACKAGE + ".*", Foo.class.getName()));
        assertTrue(matchPath(THIS_PACKAGE + ".*", Bar.class.getName()));

        // Test that a class in another package doesn't match
        assertFalse(matchPath(THIS_PACKAGE + ".*", String.class.getName()));

        // Test that a class in super package doesn't match
        assertFalse(matchPath(THIS_PACKAGE + ".*", Qux.class.getName()));

        // Test that a class in a sub package doesn't match
        assertFalse(matchPath(THIS_PACKAGE + ".*", Baz.class.getName()));

        // ---- Test Wildcard matches ----

        //Test whether classes starting with Ba match
        assertTrue(matchPath(THIS_PACKAGE + ".Ba*", Bar1.class.getName()));
        assertTrue(matchPath(THIS_PACKAGE + ".Ba*", Bar.class.getName()));
        assertFalse(matchPath(THIS_PACKAGE + ".Ba*", Foo.class.getName()));

        // ---- Test ? matches

        //Test whether three letter classes match
        assertTrue(matchPath(THIS_PACKAGE + ".???", Foo.class.getName()));
        assertTrue(matchPath(THIS_PACKAGE + ".???", Bar.class.getName()));
        assertFalse(matchPath(THIS_PACKAGE + ".???", Corge.class.getName()));

        // ---- Test deep matches ----

        //Test whether classes in the package match
        assertTrue(matchPath(THIS_PACKAGE + ".**", Foo.class.getName()));
        assertTrue(matchPath(THIS_PACKAGE + ".*", Bar.class.getName()));

        // Test that a class in another package doesn't match
        assertFalse(matchPath(THIS_PACKAGE + ".**", String.class.getName()));

        // Test that a class in super package doesn't match
        assertFalse(matchPath(THIS_PACKAGE + ".**", Qux.class.getName()));

        // Test that a class in a sub package does match
        assertTrue(matchPath(THIS_PACKAGE + ".**", Baz.class.getName()));
    }

}
