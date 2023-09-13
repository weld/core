package org.jboss.weld.bootstrap;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class BeanDeploymentArchiveMapping {

    private final Map<BeanDeploymentArchive, BeanDeployment> beanDeployments = new HashMap<BeanDeploymentArchive, BeanDeployment>();
    private final ConcurrentMap<BeanDeploymentArchive, BeanManagerImpl> beanManagers = new ConcurrentHashMap<BeanDeploymentArchive, BeanManagerImpl>();

    public void put(BeanDeploymentArchive bda, BeanDeployment beanDeployment) {
        beanDeployments.put(bda, beanDeployment);
        beanManagers.put(bda, beanDeployment.getBeanManager());
    }

    public BeanDeployment getBeanDeployment(BeanDeploymentArchive bda) {
        return beanDeployments.get(bda);
    }

    public Collection<BeanDeployment> getBeanDeployments() {
        return beanDeployments.values();
    }

    public ConcurrentMap<BeanDeploymentArchive, BeanManagerImpl> getBdaToBeanManagerMap() {
        return beanManagers;
    }

    boolean isNonuniqueIdentifierDetected() {
        Set<String> beanDeploymentArchiveIds = new HashSet<>();
        Set<String> beanManagerIds = new HashSet<>();
        for (Entry<BeanDeploymentArchive, BeanDeployment> entry : beanDeployments.entrySet()) {
            if (!beanDeploymentArchiveIds.add(entry.getKey().getId())
                    || !beanManagerIds.add(entry.getValue().getBeanManager().getId())) {
                return true;
            }
        }
        return false;
    }

}
