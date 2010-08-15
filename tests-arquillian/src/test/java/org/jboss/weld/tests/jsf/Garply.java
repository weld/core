package org.jboss.weld.tests.jsf;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;
import javax.faces.render.Renderer;

public class Garply extends UIComponent
{

   @Override
   protected void addFacesListener(FacesListener arg0)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void broadcast(FacesEvent arg0) throws AbortProcessingException
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void decode(FacesContext arg0)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void encodeBegin(FacesContext arg0) throws IOException
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void encodeChildren(FacesContext arg0) throws IOException
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void encodeEnd(FacesContext arg0) throws IOException
   {
      // TODO Auto-generated method stub

   }

   @Override
   public UIComponent findComponent(String arg0)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Map<String, Object> getAttributes()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public int getChildCount()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   @Override
   public List<UIComponent> getChildren()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getClientId(FacesContext arg0)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   protected FacesContext getFacesContext()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   protected FacesListener[] getFacesListeners(Class arg0)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public UIComponent getFacet(String arg0)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Map<String, UIComponent> getFacets()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Iterator<UIComponent> getFacetsAndChildren()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getFamily()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getId()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public UIComponent getParent()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   protected Renderer getRenderer(FacesContext arg0)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getRendererType()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public boolean getRendersChildren()
   {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public ValueBinding getValueBinding(String arg0)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public boolean isRendered()
   {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public void processDecodes(FacesContext arg0)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void processRestoreState(FacesContext arg0, Object arg1)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public Object processSaveState(FacesContext arg0)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void processUpdates(FacesContext arg0)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void processValidators(FacesContext arg0)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void queueEvent(FacesEvent arg0)
   {
      // TODO Auto-generated method stub

   }

   @Override
   protected void removeFacesListener(FacesListener arg0)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setId(String arg0)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setParent(UIComponent arg0)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setRendered(boolean arg0)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setRendererType(String arg0)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setValueBinding(String arg0, ValueBinding arg1)
   {
      // TODO Auto-generated method stub

   }

   public boolean isTransient()
   {
      // TODO Auto-generated method stub
      return false;
   }

   public void restoreState(FacesContext arg0, Object arg1)
   {
      // TODO Auto-generated method stub

   }

   public Object saveState(FacesContext arg0)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void setTransient(boolean arg0)
   {
      // TODO Auto-generated method stub

   }

}
