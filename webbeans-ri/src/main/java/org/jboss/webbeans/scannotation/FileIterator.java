package org.jboss.webbeans.scannotation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class FileIterator implements Iterator<InputStream>
{
   private List<File> files;
   private int index = 0;

   public FileIterator(File file)
   {
      files = new ArrayList<File>();
      try
      {
         create(files, file);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   protected static void create(List<File> list, File dir) throws Exception
   {
      File[] files = dir.listFiles();
      for (int i = 0; i < files.length; i++)
      {
         if (files[i].isDirectory())
         {
            create(list, files[i]);
         }
         else
         {
            list.add(files[i]);
         }
      }
   }

   public InputStream next()
   {
      if (index >= files.size()) return null;
      File fp = (File) files.get(index++);
      try
      {
         return new FileInputStream(fp);
      }
      catch (FileNotFoundException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   public boolean hasNext()
   {
      throw new UnsupportedOperationException("Cannot call hasNext() on FileIterator");
   }

   public void remove()
   {
      throw new UnsupportedOperationException("Cannot call remove() on FileIterator");
   }
   
}
