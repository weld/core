package org.jboss.weld.tests.metadata.scanning;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;

public class Utils {

    public static final String BEANS_XML_HEADER =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<beans xmlns=\"http://java.sun.com/xml/ns/javaee\" " +
                    "xmlns:weld=\"http://jboss.org/schema/weld/beans\"" + ">";

    public static final String BEANS_XML_FOOTER = "</beans>";

    public static Asset createBeansXml(String str) {
        String xml = BEANS_XML_HEADER + str + BEANS_XML_FOOTER;
        return new ByteArrayAsset(xml.getBytes());
    }

    public static String escapeClassName(Class<?> clazz) {
        return clazz.getName().replace(".", "\\.");
    }

    public static String escapePackageName(Package pkg) {
        return pkg.getName().replace(".", "\\.");
    }

}
