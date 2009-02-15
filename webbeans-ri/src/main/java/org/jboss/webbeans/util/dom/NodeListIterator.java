package org.jboss.webbeans.util.dom;

import java.util.Iterator;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NodeListIterator implements Iterator<Node>
{
   
   private final NodeList nodeList;
   private int index; 

   public NodeListIterator(NodeList nodeList)
   {
      this.nodeList = nodeList;
      index = 0;
   }

   public boolean hasNext()
   {
      return index < nodeList.getLength() - 1;
   }

   public Node next()
   {
      index++;
      return nodeList.item(index);
   }

   public void remove()
   {
      throw new UnsupportedOperationException("XML DOM is readonly");
   }
   
}
