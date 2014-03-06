package org.jboss.weld.tests.unit.serialization;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Collections;

import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.bean.StringBeanIdentifier;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.serialization.BeanIdentifierIndex;
import org.junit.Test;

public class BeanIdentifierIndexTest {

    @Test(expected=IllegalStateException.class)
    public void testIndexNotBuilt() {
        new BeanIdentifierIndex().getIdentifier(0);
    }

    @Test
    public void testInvalidIndex() {
        BeanIdentifierIndex index = new BeanIdentifierIndex();
        index.build(Collections.<Bean<?>>emptySet());
        try {
            index.getIdentifier(-10);
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }
        try {
            index.getIdentifier(0);
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }
        try {
            index.getIdentifier(10);
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    @Test
    public void testGetIndex() {
        BeanIdentifierIndex index = new BeanIdentifierIndex();
        index.build(Collections.<Bean<?>>emptySet());
        try {
            index.getIndex(null);
        } catch (IllegalArgumentException e) {
            // Expected
        }
        assertNull(index.getIndex(new StringBeanIdentifier("foo")));
    }

}
