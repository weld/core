package org.jboss.weld.bean.interceptor;

import org.jboss.interceptor.reader.cache.DefaultMetadataCachingReader;
import org.jboss.weld.bootstrap.api.Service;

public class InterceptorMetadataCacheService extends DefaultMetadataCachingReader implements Service {
    public void cleanup() {

    }
}
