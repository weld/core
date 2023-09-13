package org.jboss.weld.environment.servlet.test.util;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public final class TomcatDeployments {

    private TomcatDeployments() {
    }

    public static final Asset CONTEXT_XML = new StringAsset(
            "<Context crossContext=\"true\">" +
                    "<Manager pathname=\"\" />" +
                    "<Resource name=\"BeanManager\" auth=\"Container\" type=\"jakarta.inject.manager.BeanManager\" factory=\"org.jboss.weld.resources.ManagerObjectFactory\"/>"
                    +
                    "<Environment name=\"foo\" value=\"bar\" type=\"java.lang.String\"/>" +
                    "<JarScanner scanManifest=\"false\"/>" +
                    "</Context>");

    public static <W extends WebArchive> W apply(W archive) {
        archive.add(CONTEXT_XML, "META-INF/context.xml");
        return archive;
    }

}
