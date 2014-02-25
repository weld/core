package org.jboss.weld.environment.se.discovery.url;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.logging.Logger;
import org.jboss.weld.bootstrap.api.Bootstrap;


public class JandexEnabledFileSystemURLHandler extends FileSystemURLHandler {

    protected static final Logger log = Logger.getLogger(JandexEnabledFileSystemURLHandler.class);
    private Indexer indexer = new Indexer();

    public JandexEnabledFileSystemURLHandler(String id, Bootstrap bootstrap) {
        super(id, bootstrap);
    }

    private void addToIndex(URL url) {
        InputStream fs = null;
        try {
            fs = url.openStream();
            indexer.index(fs);
        } catch (IOException ex) {

        } finally {
            try {
                if (fs != null) {
                    fs.close();
                }
            } catch (IOException ex) {
            }
        }
    }

    @Override
    protected void addToDiscovered(String name, URL url) {
        super.addToDiscovered(name, url);
        if (name.endsWith(CLASS_FILE_EXTENSION)) {
            addToIndex(url);
        }
    }

    @Override
    protected BeanArchiveBuilder createBeanArchiveBuilder() {
        return new BeanArchiveBuilder(this.getId(), buildJandexIndex(), getDiscoveredClasses(), getDiscoveredBeansXmlUrl(), getBootstrap());
    }

    public Index buildJandexIndex() {
        return indexer.complete();
    }
}
