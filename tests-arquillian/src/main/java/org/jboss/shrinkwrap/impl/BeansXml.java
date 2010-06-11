package org.jboss.shrinkwrap.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.shrinkwrap.api.Asset;

class BeansXml implements Asset
{
  private List<Class<?>> alternatives = new ArrayList<Class<?>>();
  private List<Class<?>> interceptors = new ArrayList<Class<?>>();
  private List<Class<?>> decorators = new ArrayList<Class<?>>();
  private List<Class<?>> stereotypes = new ArrayList<Class<?>>();
  
  BeansXml() {

  }

  public BeansXml alternatives(Class<?>... alternatives)
  {
     this.alternatives.addAll(Arrays.asList(alternatives));
     return this;
  }

  public BeansXml interceptors(Class<?>... interceptors)
  {
     this.interceptors.addAll(Arrays.asList(interceptors));
     return this;
  }

  public BeansXml decorators(Class<?>... decorators)
  {
     this.decorators.addAll(Arrays.asList(decorators));
     return this;
  }

  public BeansXml stereotype(Class<?>... stereotypes)
  {
     this.stereotypes.addAll(Arrays.asList(stereotypes));
     return this;
  }
  
  public InputStream openStream()
  {
     StringBuilder xml = new StringBuilder();
     xml.append("<beans>\n");
     appendAlternatives(alternatives, stereotypes, xml);
     appendSection("interceptors", "class", interceptors, xml);
     appendSection("decorators", "class", decorators, xml);
     xml.append("</beans>");

     return new ByteArrayInputStream(xml.toString().getBytes());
  }

  private void appendAlternatives(List<Class<?>> alternatives, List<Class<?>> stereotypes, StringBuilder xml)
  {
     if(alternatives.size() > 0 || stereotypes.size() > 0)
     {
        xml.append("<").append("alternatives").append(">\n");
        appendClasses("class", alternatives, xml);
        appendClasses("stereotype", stereotypes, xml);
        xml.append("</").append("alternatives").append(">\n");
     }
  }
  
  private void appendSection(String name, String subName, List<Class<?>> classes, StringBuilder xml)
  {
     if(classes.size() > 0)
     {
        xml.append("<").append(name).append(">\n");
        appendClasses(subName, classes, xml);
        xml.append("</").append(name).append(">\n");
     }
  }

  private void appendClasses(String name, List<Class<?>> classes, StringBuilder xml)
  {
     for(Class<?> clazz : classes)
     {
        xml.append("<").append(name).append(">")
              .append(clazz.getName())
              .append("</").append(name).append(">\n");
     }
  }
}
