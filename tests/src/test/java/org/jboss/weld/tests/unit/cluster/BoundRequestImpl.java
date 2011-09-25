package org.jboss.weld.tests.unit.cluster;

import org.jboss.weld.context.bound.BoundRequest;

import java.util.HashMap;
import java.util.Map;

public class BoundRequestImpl implements BoundRequest {

    private final Map<String, Object> requestMap;
    private final Map<String, Object> sessionMap;


    public BoundRequestImpl(Map<String, Object> sessionMap) {
        this.requestMap = new HashMap<String, Object>();
        this.sessionMap = sessionMap;
    }

    public Map<String, Object> getRequestMap() {
        return requestMap;
    }

    public Map<String, Object> getSessionMap(boolean create) {
        return sessionMap;
    }

}
