package org.jboss.webbeans.scannotation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class JarIterator implements Iterator<InputStream>
{
   JarInputStream jar;
   JarEntry next;
   boolean initial = true;
   boolean closed = false;

   public JarIterator(File file) throws IOException
   {
      this(new FileInputStream(file));
   }


   public JarIterator(InputStream is) throws IOException
   {
      jar = new JarInputStream(is);
   }

   private void setNext()
   {
      initial = true;
      try
      {
         if (next != null)
         {
            jar.closeEntry();
         }
         next = null;
         do
         {
            next = jar.getNextJarEntry();
         }
         while (next != null && next.isDirectory());
         if (next == null)
         {
            close();
         }
      }
      catch (IOException e)
      {
         throw new RuntimeException("failed to browse jar", e);
      }
   }

   public InputStream next()
   {
      if (closed || (next == null && !initial)) 
      {
         return null;
      }
      setNext();
      if (next == null)
      {
         return null;
      }
      return new InputStreamWrapper(jar);
   }

   private void close()
   {
      try
      {
         closed = true;
         jar.close();
      }
      catch (IOException ignored)
      {

      }

   }


   public boolean hasNext()
   {
      throw new UnsupportedOperationException("Cannot call hasNext() on a JarIterator");
   }


   public void remove()
   {
      throw new UnsupportedOperationException("Cannot call remove() on a JarIterator");
   }
}
