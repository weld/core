package org.jboss.weld.environment.se.discovery.url;

public interface URLHandler {

    BeanArchiveBuilder handle(String urlPath);
}
