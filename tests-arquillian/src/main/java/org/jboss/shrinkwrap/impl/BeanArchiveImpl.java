package org.jboss.shrinkwrap.impl;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.BeansXmlClass;
import org.jboss.shrinkwrap.impl.base.path.BasicPath;
import org.jboss.shrinkwrap.impl.base.spec.JavaArchiveImpl;

public class BeanArchiveImpl extends JavaArchiveImpl implements BeanArchive {
    // -------------------------------------------------------------------------------------||
    // Class Members ----------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    /**
     * Path to the manifests inside of the Archive.
     */
    private static final ArchivePath PATH_MANIFEST = new BasicPath("META-INF");

    /**
     * Path to the resources inside of the Archive.
     */
    private static final ArchivePath PATH_RESOURCE = new BasicPath("/");

    /**
     * Path to the classes inside of the Archive.
     */
    private static final ArchivePath PATH_CLASSES = new BasicPath("/");

    /**
     * Beans XML object
     */
    private BeansXml descriptor;

    public BeanArchiveImpl(final Archive<?> delegate) {
        super(delegate);

        // add beans.xml descriptor
        descriptor = new BeansXml();
        addAsManifestResource(descriptor, ArchivePaths.create("beans.xml"));
    }

    // -------------------------------------------------------------------------------------||
    // Required Implementations -----------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    // -------------------------------------------------------------------------------------||
    // Required Implementations - BeanArchive ---------------------------------------------||
    // -------------------------------------------------------------------------------------||

    public BeanArchive decorate(Class<?>... classes) {
        descriptor.decorators(classes);
        addClasses(classes);
        return covarientReturn();
    }

    @Override
    public BeanArchive decorate(BeansXmlClass... classes) {
        descriptor.decorators(classes);
        return covarientReturn();
    }

    public BeanArchive intercept(Class<?>... classes) {
        descriptor.interceptors(classes);
        addClasses(classes);
        return covarientReturn();
    }

    @Override
    public BeanArchive intercept(BeansXmlClass... classes) {
        descriptor.interceptors(classes);
        return covarientReturn();
    }

    public BeanArchive alternate(Class<?>... classes) {
        descriptor.alternatives(classes);
        addClasses(classes);
        return covarientReturn();
    }

    @Override
    public BeanArchive alternate(BeansXmlClass... classes) {
        descriptor.alternatives(classes);
        return covarientReturn();
    }

    public BeanArchive stereotype(Class<?>... classes) {
        descriptor.stereotypes(classes);
        addClasses(classes);
        return covarientReturn();
    }

    @Override
    protected BeanArchive covarientReturn() {
        return (BeanArchive) super.covarientReturn();
    }
}
