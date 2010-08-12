package org.jboss.shrinkwrap.impl;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.impl.base.path.BasicPath;
import org.jboss.shrinkwrap.impl.base.spec.JavaArchiveImpl;

public class BeanArchiveImpl extends JavaArchiveImpl implements BeanArchive
{
   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

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

   public BeanArchiveImpl(final Archive<?> delegate)
   {
      super(delegate);

      // add beans.xml descriptor
      descriptor = new BeansXml();
      addManifestResource(descriptor, ArchivePaths.create("beans.xml"));
   }

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   @Override
   protected ArchivePath getManifestPath()
   {
      return PATH_MANIFEST;
   }

   @Override
   protected ArchivePath getClassesPath()
   {
      return PATH_CLASSES;
   }

   @Override
   protected ArchivePath getResourcePath()
   {
      return PATH_RESOURCE;
   }

   /**
    * Libraries are not supported by JavaArchive.
    * 
    * @throws UnsupportedOperationException Libraries are not supported by
    *            JavaArchive
    */
   @Override
   public ArchivePath getLibraryPath()
   {
      throw new UnsupportedOperationException("JavaArchive spec does not support Libraries");
   }

   //-------------------------------------------------------------------------------------||
   // Required Implementations - BeanArchive ---------------------------------------------||
   //-------------------------------------------------------------------------------------||

   public BeanArchive decorate(Class<?>... classes)
   {
      descriptor.decorators(classes);
      addClasses(classes);
      return covarientReturn();
   }

   public BeanArchive intercept(Class<?>... classes)
   {
      descriptor.interceptors(classes);
      addClasses(classes);
      return covarientReturn();
   }
   
   public BeanArchive alternate(Class<?>... classes)
   {
      descriptor.alternatives(classes);
      addClasses(classes);
      return covarientReturn();
   }

   public BeanArchive stereotype(Class<?>... classes)
   {
      descriptor.stereotype(classes);
      addClasses(classes);
      return covarientReturn();
   }

   @Override
   protected BeanArchive covarientReturn()
   {
      return (BeanArchive) super.covarientReturn();
   }
}