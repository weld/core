package org.jboss.webbeans.xsd;

import java.util.List;

import org.dom4j.Document;

public class PackageInfo
{
   private List<String> namespaces;
   private Document schema;
   private String packageName;

   public PackageInfo(String packageName)
   {
      this.packageName = packageName;
   }

   public List<String> getNamespaces()
   {
      return namespaces;
   }

   public void setNamespaces(List<String> namespaces)
   {
      this.namespaces = namespaces;
   }

   public Document getSchema()
   {
      return schema;
   }

   public void setSchema(Document schema)
   {
      this.schema = schema;
   }

   public String getPackageName()
   {
      return packageName;
   }

   public void setPackageName(String packageName)
   {
      this.packageName = packageName;
   }
   
}
