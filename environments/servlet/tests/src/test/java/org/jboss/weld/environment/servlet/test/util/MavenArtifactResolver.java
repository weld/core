package org.jboss.weld.environment.servlet.test.util;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves a maven artifact present on the test classpath.
 * 
 * @author Stuart Douglas
 * 
 */
public class MavenArtifactResolver
{
   public static File resolve(String groupId, String artifactId)
   {
      String classPath = System.getProperty("java.class.path");
      // first look for an artefact from the repo
      String pathString = groupId.replace('.', File.separatorChar) + File.separatorChar + artifactId;
      String regex = "[^:]*" + Pattern.quote(pathString) + "[^:]*";
      Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
      Matcher matches = p.matcher(classPath);

      if (!matches.find())
      {
         // find a resource from the local build
         String localResource = Pattern.quote("target" + File.separatorChar + artifactId);
         regex = "[^:]*" + localResource + "[^:]*";
         p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
         matches = p.matcher(classPath);
         if (!matches.find())
         {
            throw new RuntimeException("Unable to find maven archive " + groupId + ":" + artifactId + " on the test classpath");
         }
      }
      return new File(matches.group(0));
   }

   public static File resolve(String qualifiedArtifactId)
   {
      String[] segments = qualifiedArtifactId.split(":");
      return resolve(segments[0], segments[1]);
   }
}
