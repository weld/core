package org.jboss.webbeans.scannotation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class IteratorFactory
{

   public static Iterator<InputStream> create(URL url) throws IOException
   {
      String urlString = url.toString();
      if (urlString.endsWith("!/"))
      {
         urlString = urlString.substring(4);
         urlString = urlString.substring(0, urlString.length() - 2);
         url = new URL(urlString);
      }


      if (!urlString.endsWith("/"))
      {
         return new JarIterator(url.openStream());
      }
      else
      {
         File f = new File(url.getPath());
         if (f.isDirectory())
         {
            return new FileIterator(f);
         }
         else
         {
            return new JarIterator(url.openStream());
         }
      }
   }
}
