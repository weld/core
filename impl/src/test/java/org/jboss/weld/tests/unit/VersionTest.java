package org.jboss.weld.tests.unit;

import static org.junit.Assert.assertEquals;

import org.jboss.weld.util.reflection.Formats;
import org.junit.Test;

public class VersionTest {

    @Test
    public void testVersionParser() {
        assertEquals("1.0.0 (1981-28-12 17:00)", Formats.version("1.0.0-SNAPSHOT", "1981-28-12 17:00"));
        assertEquals("1.0 (1981-28-12 17:00)", Formats.version("1.0-SNAPSHOT", "1981-28-12 17:00"));
        assertEquals("1 (1981-28-12 17:00)", Formats.version("1-SNAPSHOT", "1981-28-12 17:00"));
        assertEquals("1.0.0 (SNAPSHOT)", Formats.version("1.0.0-SNAPSHOT", null));
        assertEquals("1.0 (SNAPSHOT)", Formats.version("1.0-SNAPSHOT", null));
        assertEquals("1 (SNAPSHOT)", Formats.version("1-SNAPSHOT", null));

        assertEquals("1.0.0 (BETA1)", Formats.version("1.0.0.BETA1", "1981-28-12 17:00"));
        assertEquals("1.0 (BETA1)", Formats.version("1.0.BETA1", "1981-28-12 17:00"));
        assertEquals("1 (BETA1)", Formats.version("1.BETA1", "1981-28-12 17:00"));
        assertEquals("1.0.0 (BETA1)", Formats.version("1.0.0.BETA1", null));
        assertEquals("1.0 (BETA1)", Formats.version("1.0.BETA1", null));
        assertEquals("1 (BETA1)", Formats.version("1.BETA1", null));

        assertEquals("1.0.0 (BETA1)", Formats.version("1.0.0-BETA1", "1981-28-12 17:00"));
        assertEquals("1.0 (BETA1)", Formats.version("1.0-BETA1", "1981-28-12 17:00"));
        assertEquals("1 (BETA1)", Formats.version("1-BETA1", "1981-28-12 17:00"));
        assertEquals("1.0.0 (BETA1)", Formats.version("1.0.0-BETA1", null));
        assertEquals("1.0 (BETA1)", Formats.version("1.0-BETA1", null));
        assertEquals("1 (BETA1)", Formats.version("1-BETA1", null));
    }

}
