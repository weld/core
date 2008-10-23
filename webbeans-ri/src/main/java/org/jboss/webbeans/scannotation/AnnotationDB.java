package org.jboss.webbeans.scannotation;


import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;

/**
 * The class allows you to scan an arbitrary set of "archives" for .class files.  These class files
 * are parsed to see what annotations they use.  Two indexes are created.  The javax, java, sun, com.sun, and javassist
 * packages will not be scanned by default.
 * <p/>
 * One is a map of annotations and what classes
 * use those annotations.   This could be used, for example, by an EJB deployer to find all the EJBs contained
 * in the archive
 * <p/>
 * Another is a mpa of classes and what annotations those classes use.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author Pete Muir
 * @version $Revision: 1 $
 */
public abstract class AnnotationDB implements Serializable
{
   protected Map<String, Set<String>> annotationIndex = new HashMap<String, Set<String>>();
   protected Map<String, Set<String>> implementsIndex = new HashMap<String, Set<String>>();
   protected Map<String, Set<String>> classIndex = new HashMap<String, Set<String>>();

   protected transient boolean scanClassAnnotations = true;
   protected transient boolean scanMethodAnnotations = true;
   protected transient boolean scanParameterAnnotations = true;
   protected transient boolean scanFieldAnnotations = true;

   public class CrossReferenceException extends Exception
   {
      private Set<String> unresolved;

      public CrossReferenceException(Set<String> unresolved)
      {
         this.unresolved = unresolved;
      }

      public Set<String> getUnresolved()
      {
         return unresolved;
      }
   }

   /**
    * This method will cross reference annotations in the annotation index with any meta-annotations that they have
    * and create additional entries as needed.  For example:
    *
    * @HttpMethod("GET") public @interface GET {}
    * <p/>
    * The HttpMethod index will have additional classes added to it for any classes annotated with annotations that
    * have the HttpMethod meta-annotation.
    * <p/>
    * WARNING: If the annotation class has not already been scaned, this method will load all annotation classes indexed
    * as a resource so they must be in your classpath
    */
   public void crossReferenceMetaAnnotations() throws CrossReferenceException
   {
      Set<String> unresolved = new HashSet<String>();

      Set<String> index = new HashSet<String>();
      index.addAll(annotationIndex.keySet());

      for (String annotation : index)
      {
         if (classIndex.containsKey(annotation))
         {
            for (String xref : classIndex.get(annotation))
            {
               annotationIndex.get(xref).addAll(annotationIndex.get(annotation));
            }
            continue;
         }
         InputStream bits = Thread.currentThread().getContextClassLoader().getResourceAsStream(annotation.replace('.', '/') + ".class");
         if (bits == null)
         {
            unresolved.add(annotation);
            continue;
         }
         boolean scanned = false;
         try
         {
            scanned = scanClass(bits);
         }
         catch (IOException e)
         {
            unresolved.add(annotation);
         }
         if (scanned)
         {
            for (String xref : classIndex.get(annotation))
            {
               annotationIndex.get(xref).addAll(annotationIndex.get(annotation));
            }
         }

      }
      if (unresolved.size() > 0) throw new CrossReferenceException(unresolved);
   }

   /**
    * Sometimes you want to see if a particular class implements an interface with certain annotations
    * After you have loaded all your classpaths with the scanArchive() method, call this method to cross reference
    * a class's implemented interfaces.  The cross references will be added to the annotationIndex and
    * classIndex indexes
    *
    * @param ignoredPackages var arg list of packages to ignore
    * @throws CrossReferenceException an Exception thrown if referenced interfaces haven't been scanned
    */
   public void crossReferenceImplementedInterfaces() throws CrossReferenceException
   {
      Set<String> unresolved = new HashSet<String>();
      for (String clazz : implementsIndex.keySet())
      {
         Set<String> intfs = implementsIndex.get(clazz);
         for (String intf : intfs)
         {

            Set<String> xrefAnnotations = classIndex.get(intf);
            if (xrefAnnotations == null)
            {
               unresolved.add(intf);
            }
            else
            {
               Set<String> classAnnotations = classIndex.get(clazz);
               if (classAnnotations == null)
               {
                  classIndex.put(clazz, xrefAnnotations);
               }
               else classAnnotations.addAll(xrefAnnotations);
               for (String annotation : xrefAnnotations)
               {
                  Set<String> classes = annotationIndex.get(annotation);
                  classes.add(clazz);
               }
            }
         }
      }
      if (unresolved.size() > 0) throw new CrossReferenceException(unresolved);

   }
   
   protected abstract boolean ignoreScan(ClassFile classFile);

   /**
    * returns a map keyed by the fully qualified string name of a annotation class.  The Set returne is
    * a list of classes that use that annotation somehow.
    */
   public Map<String, Set<String>> getAnnotationIndex()
   {
      return annotationIndex;
   }

   /**
    * returns a map keyed by the list of classes scanned.  The value set returned is a list of annotations
    * used by that class.
    */
   public Map<String, Set<String>> getClassIndex()
   {
      return classIndex;
   }


   /**
    * Whether or not you want AnnotationDB to scan for class level annotations
    *
    * @param scanClassAnnotations
    */
   public void setScanClassAnnotations(boolean scanClassAnnotations)
   {
      this.scanClassAnnotations = scanClassAnnotations;
   }

   /**
    * Wheter or not you want AnnotationDB to scan for method level annotations
    *
    * @param scanMethodAnnotations
    */
   public void setScanMethodAnnotations(boolean scanMethodAnnotations)
   {
      this.scanMethodAnnotations = scanMethodAnnotations;
   }

   /**
    * Whether or not you want AnnotationDB to scan for parameter level annotations
    *
    * @param scanParameterAnnotations
    */
   public void setScanParameterAnnotations(boolean scanParameterAnnotations)
   {
      this.scanParameterAnnotations = scanParameterAnnotations;
   }

   /**
    * Whether or not you want AnnotationDB to scan for parameter level annotations
    *
    * @param scanFieldAnnotations
    */
   public void setScanFieldAnnotations(boolean scanFieldAnnotations)
   {
      this.scanFieldAnnotations = scanFieldAnnotations;
   }


   /**
    * Scan a url that represents an "archive"  this is a classpath directory or jar file
    *
    * @param urls variable list of URLs to scan as archives
    * @throws IOException
    */
   public void scanArchives(URL... urls) throws IOException
   {
      for (URL url : urls)
      {
         Iterator<InputStream> it = IteratorFactory.create(url);

         InputStream stream;
         while ((stream = it.next()) != null) scanClass(stream);
      }

   }

   /**
    * Parse a .class file for annotations
    *
    * @param bits input stream pointing to .class file bits
    * @throws IOException
    */
   public boolean scanClass(InputStream bits) throws IOException
   {
      DataInputStream dstream = new DataInputStream(new BufferedInputStream(bits));
      ClassFile cf = null;
      try
      {
         cf = new ClassFile(dstream);
         if (!ignoreScan(cf))
         {
            classIndex.put(cf.getName(), new HashSet<String>());
            if (scanClassAnnotations) ;
            scanClass(cf);
            if (scanMethodAnnotations || scanParameterAnnotations) scanMethods(cf);
            if (scanFieldAnnotations) scanFields(cf);
   
            // create an index of interfaces the class implements
            if (cf.getInterfaces() != null)
            {
               Set<String> intfs = new HashSet<String>();
               for (String intf : cf.getInterfaces()) intfs.add(intf);
               implementsIndex.put(cf.getName(), intfs);
            }
            return true;
         }
         else
         {
            return false;
         }

      }
      finally
      {
         dstream.close();
         bits.close();
      }
   }

   protected void scanClass(ClassFile cf)
   {
      String className = cf.getName();
      AnnotationsAttribute visible = (AnnotationsAttribute) cf.getAttribute(AnnotationsAttribute.visibleTag);
      AnnotationsAttribute invisible = (AnnotationsAttribute) cf.getAttribute(AnnotationsAttribute.invisibleTag);

      if (visible != null) populate(visible.getAnnotations(), className);
      if (invisible != null) populate(invisible.getAnnotations(), className);
   }

   /**
    * Scanns both the method and its parameters for annotations.
    *
    * @param cf
    */
   @SuppressWarnings("unchecked")
   protected void scanMethods(ClassFile cf)
   {
      List methods = cf.getMethods();
      if (methods == null) return;
      for (Object obj : methods)
      {
         MethodInfo method = (MethodInfo) obj;
         if (scanMethodAnnotations)
         {
            AnnotationsAttribute visible = (AnnotationsAttribute) method.getAttribute(AnnotationsAttribute.visibleTag);
            AnnotationsAttribute invisible = (AnnotationsAttribute) method.getAttribute(AnnotationsAttribute.invisibleTag);

            if (visible != null) populate(visible.getAnnotations(), cf.getName());
            if (invisible != null) populate(invisible.getAnnotations(), cf.getName());
         }
         if (scanParameterAnnotations)
         {
            ParameterAnnotationsAttribute paramsVisible = (ParameterAnnotationsAttribute) method.getAttribute(ParameterAnnotationsAttribute.visibleTag);
            ParameterAnnotationsAttribute paramsInvisible = (ParameterAnnotationsAttribute) method.getAttribute(ParameterAnnotationsAttribute.invisibleTag);

            if (paramsVisible != null && paramsVisible.getAnnotations() != null)
            {
               for (Annotation[] anns : paramsVisible.getAnnotations())
               {
                  populate(anns, cf.getName());
               }
            }
            if (paramsInvisible != null && paramsInvisible.getAnnotations() != null)
            {
               for (Annotation[] anns : paramsInvisible.getAnnotations())
               {
                  populate(anns, cf.getName());
               }
            }
         }
      }
   }

   @SuppressWarnings("unchecked")
   protected void scanFields(ClassFile cf)
   {
      List fields = cf.getFields();
      if (fields == null) return;
      for (Object obj : fields)
      {
         FieldInfo field = (FieldInfo) obj;
         AnnotationsAttribute visible = (AnnotationsAttribute) field.getAttribute(AnnotationsAttribute.visibleTag);
         AnnotationsAttribute invisible = (AnnotationsAttribute) field.getAttribute(AnnotationsAttribute.invisibleTag);

         if (visible != null) populate(visible.getAnnotations(), cf.getName());
         if (invisible != null) populate(invisible.getAnnotations(), cf.getName());
      }


   }

   protected void populate(Annotation[] annotations, String className)
   {
      if (annotations == null) return;
      Set<String> classAnnotations = classIndex.get(className);
      for (Annotation ann : annotations)
      {
         Set<String> classes = annotationIndex.get(ann.getTypeName());
         if (classes == null)
         {
            classes = new HashSet<String>();
            annotationIndex.put(ann.getTypeName(), classes);
         }
         classes.add(className);
         classAnnotations.add(ann.getTypeName());
      }
   }

   /**
    * Prints out annotationIndex
    *
    * @param writer
    */
   public void outputAnnotationIndex(PrintWriter writer)
   {
      for (String ann : annotationIndex.keySet())
      {
         writer.print(ann);
         writer.print(": ");
         Set<String> classes = annotationIndex.get(ann);
         Iterator<String> it = classes.iterator();
         while (it.hasNext())
         {
            writer.print(it.next());
            if (it.hasNext()) writer.print(", ");
         }
         writer.println();
      }
   }

}
