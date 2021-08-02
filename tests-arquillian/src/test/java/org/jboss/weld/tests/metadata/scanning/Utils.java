package org.jboss.weld.tests.metadata.scanning;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;

public class Utils {

    public static final String BEANS_XML_HEADER =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<beans xmlns=\"https://jakarta.ee/xml/ns/jakartaee\" " +
                    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                    "xmlns:weld=\"http://jboss.org/schema/weld/beans\" " +
                    "xsi:schemaLocation=\"https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/beans_3_0.xsd\" " +
                    "version=\"3.0\" bean-discovery-mode=\"all\">";



    public static final String BEANS_XML_FOOTER = "</beans>";

    /**
     * Note that this util method is delierately creating a beans.xml file with discovery mode ALL.
     */
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
