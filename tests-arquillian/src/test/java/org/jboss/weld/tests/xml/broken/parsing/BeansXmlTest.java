package org.jboss.weld.tests.xml.broken.parsing;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class BeansXmlTest {
    @Deployment //@Expected(Exception.class)
    public static Archive<?> deploy() {
        return ShrinkWrap.create(JavaArchive.class)
                .addPackage(BeansXmlTest.class.getPackage())
                .addAsManifestResource(
                        new StringAsset(
                                "<beans xmlns=\"http://java.sun.com/xml/ns/javaee\" xmlns:weld=\"http://jboss.org/schema/weld/beans\">\n"
                                        +
                                        "   <alternatives>\n" +
                                        "      <foo></foo>\n" +
                                        "   </alternatives>\n" +
                                        "   <weld:scan>\n" +
                                        "    <bar></bar>\n" +
                                        "   </weld:scan>\n" +
                                        "</beans>\n" +
                                        ""),
                        "beans.xml");
    }

    @Test
    public void test() {
        //assert false;
    }

}
