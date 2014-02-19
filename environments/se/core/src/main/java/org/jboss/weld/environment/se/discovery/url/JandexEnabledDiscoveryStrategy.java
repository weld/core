package org.jboss.weld.environment.se.discovery.url;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.environment.se.discovery.WeldSEBeanDeploymentArchive;
import org.jboss.weld.resources.spi.ResourceLoader;

public class JandexEnabledDiscoveryStrategy extends DiscoveryStrategy {

    private static final String SOME_SESSION_CLASS = "someClass";
    CompositeIndex cindex;

    public JandexEnabledDiscoveryStrategy(ResourceLoader resourceLoader, Bootstrap bootstrap) {
        super(resourceLoader, bootstrap);
    }

    protected void initialize() {
        List<IndexView> indexes = new ArrayList<IndexView>();
        for (BeanArchiveBuilder builder : getBuilders()) {
            IndexView index = (IndexView) builder.getIndex();
            indexes.add(index);
        }
        cindex = CompositeIndex.create(indexes);
    }

    protected void manageAnnotatedDiscovery(BeanArchiveBuilder builder) {
        for (String className : builder.getClasses()) {
            ClassInfo cinfo = cindex.getClassByName(DotName.createSimple(className));
            if (!containsBeanDefiningAnnotation(cinfo.annotations().keySet())) {
                builder.removeClass(className);
            }
        }
        WeldSEBeanDeploymentArchive bda = builder.build();
        addToArchives(bda);
    }

    private boolean containsBeanDefiningAnnotation(Set<DotName> annotations) {
        for (DotName name : annotations) {
            if (name.toString().equals(SOME_SESSION_CLASS)) {
                return true;
            }
        }
        return false;
    }

}
