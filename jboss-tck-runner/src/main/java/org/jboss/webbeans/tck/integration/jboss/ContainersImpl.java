package org.jboss.webbeans.tck.integration.jboss;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jboss.jsr299.tck.spi.Containers;


public class ContainersImpl implements Containers
{
   
   public static final String JBOSS_HOME = "/Applications/jboss-5.0.0.GA";
   
   private final File deployDir;
   
   public ContainersImpl()
   {
      deployDir = new File(JBOSS_HOME, "server/default/deploy");
      if (!deployDir.isDirectory())
      {
         throw new IllegalArgumentException(deployDir.getPath() + " is not a directory"); 
      }
   }
   
   public void deploy(InputStream archive, String name) throws Exception
   {
      File file = new File(deployDir, name);
      file.createNewFile();
      copy(archive, file);
   }
   
   private static void copy(InputStream inputStream, File file) throws IOException
   {
      OutputStream os = new FileOutputStream(file);
      try 
      {
         byte[] buf = new byte[1024];
         int i = 0;
         while ((i = inputStream.read(buf)) != -1) 
         {
             os.write(buf, 0, i);
         }
     } 
     finally 
     {
         os.close();
     }
   }
   
}
