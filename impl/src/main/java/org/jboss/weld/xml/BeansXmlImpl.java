package org.jboss.weld.xml;

import java.util.List;

import org.jboss.weld.bootstrap.spi.BeansXml;

public class BeansXmlImpl implements BeansXml
{

   private final List<String> enabledAlternativeClasses;
   private final List<String> enabledAlternativeStereotypes;
   private final List<String> enabledDecorators;
   private final List<String> enabledInterceptors;
   
   public BeansXmlImpl(List<String> enabledAlternativeClasses, List<String> enabledAlternativeStereotypes, List<String> enabledDecorators, List<String> enabledInterceptors)
   {
      this.enabledAlternativeClasses = enabledAlternativeClasses;
      this.enabledAlternativeStereotypes = enabledAlternativeStereotypes;
      this.enabledDecorators = enabledDecorators;
      this.enabledInterceptors = enabledInterceptors;
   }

   public List<String> getEnabledAlternativeClasses()
   {
      return enabledAlternativeClasses;
   }

   public List<String> getEnabledAlternativeStereotypes()
   {
      return enabledAlternativeStereotypes;
   }

   public List<String> getEnabledDecorators()
   {
      return enabledDecorators;
   }

   public List<String> getEnabledInterceptors()
   {
      return enabledInterceptors;
   }

}
