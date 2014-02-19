package org.jboss.weld.environment.se.discovery.url;

import java.net.URL;
import java.util.List;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.environment.se.discovery.WeldSEBeanDeploymentArchive;

public class BeanArchiveBuilder {

    private Object index;
    private List<String> classes;
    private URL beansXmlUrl;
    private BeansXml beansXml = null;
    private Bootstrap bootstrap;
    private String id;

    public Object getIndex() {
        return index;
    }

    public List<String> getClasses() {
        return classes;
    }

    public URL getBeansXmlUrl() {
        return beansXmlUrl;
    }


    public BeanArchiveBuilder(String id, Object index, List<String> classes, URL beansXmlUrl, Bootstrap bootstrap) {
        this.id = id;
        this.index = index;
        this.classes = classes;
        this.beansXmlUrl = beansXmlUrl;
        this.bootstrap = bootstrap;
    }

    public BeansXml parseBeansXml() {
        beansXml = bootstrap.parse(beansXmlUrl);
        return beansXml;
    }

    public BeansXml getParsedBeansXml() {
        if (beansXml == null) {
            return parseBeansXml();
        } else {
            return beansXml;
        }
    }

    public WeldSEBeanDeploymentArchive build() {
        if (beansXml == null) {
            parseBeansXml();
        }
        return new WeldSEBeanDeploymentArchive(id, classes, beansXml);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void clearClasses() {
        classes.clear();
    }

    public void removeClass(String className) {
        classes.remove(className);
    }


}
