package org.jboss.weld.tests.jsf;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.FacesEvent;
import jakarta.faces.event.FacesListener;
import jakarta.faces.render.Renderer;

public class Garply extends UIComponent {

    @Override
    protected void addFacesListener(FacesListener arg0) {
    }

    @Override
    public void broadcast(FacesEvent arg0) throws AbortProcessingException {
    }

    @Override
    public void decode(FacesContext arg0) {
    }

    @Override
    public void encodeBegin(FacesContext arg0) throws IOException {
    }

    @Override
    public void encodeChildren(FacesContext arg0) throws IOException {
    }

    @Override
    public void encodeEnd(FacesContext arg0) throws IOException {
    }

    @Override
    public UIComponent findComponent(String arg0) {
        return null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public List<UIComponent> getChildren() {
        return null;
    }

    @Override
    public String getClientId(FacesContext arg0) {
        return null;
    }

    @Override
    protected FacesContext getFacesContext() {
        return null;
    }

    @Override
    protected FacesListener[] getFacesListeners(Class arg0) {
        return null;
    }

    @Override
    public UIComponent getFacet(String arg0) {
        return null;
    }

    @Override
    public Map<String, UIComponent> getFacets() {
        return null;
    }

    @Override
    public Iterator<UIComponent> getFacetsAndChildren() {
        return null;
    }

    @Override
    public String getFamily() {
        return null;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public UIComponent getParent() {
        return null;
    }

    @Override
    protected Renderer getRenderer(FacesContext arg0) {
        return null;
    }

    @Override
    public String getRendererType() {
        return null;
    }

    @Override
    public boolean getRendersChildren() {
        return false;
    }

    @Override
    public boolean isRendered() {
        return false;
    }

    @Override
    public void processDecodes(FacesContext arg0) {
    }

    @Override
    public void processRestoreState(FacesContext arg0, Object arg1) {


    }

    @Override
    public Object processSaveState(FacesContext arg0) {
        return null;
    }

    @Override
    public void processUpdates(FacesContext arg0) {
    }

    @Override
    public void processValidators(FacesContext arg0) {
    }

    @Override
    public void queueEvent(FacesEvent arg0) {
    }

    @Override
    protected void removeFacesListener(FacesListener arg0) {
    }

    @Override
    public void setId(String arg0) {
    }

    @Override
    public void setParent(UIComponent arg0) {
    }

    @Override
    public void setRendered(boolean arg0) {
    }

    @Override
    public void setRendererType(String arg0) {
    }

    public boolean isTransient() {
        return false;
    }

    public void restoreState(FacesContext arg0, Object arg1) {
    }

    public Object saveState(FacesContext arg0) {
        return null;
    }

    public void setTransient(boolean arg0) {
    }

}
