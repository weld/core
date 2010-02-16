package org.jboss.weld.examples.pastecode.session;

import java.util.List;
import java.util.ArrayList;

public class QueryInfo
{
   private int recordsCount = 0;
   private int pagesCount = 0;
   private int numLinks = 8;
   private int startIndex;
   private int endIndex;
   private int page = 0;
   private List<Integer> indexes;

   public int getNumLinks()
   {
      return numLinks;
   }

   public int getPage()
   {
      return page;
   }

   public void setPage(int page)
   {
      this.page = page;
   }

   public int getPagesCount()
   {
      return pagesCount;
   }

   public void setPagesCount(int pagesCount)
   {
      this.pagesCount = pagesCount;

      if (pagesCount == 1)
      {
         this.setBoundedIndexes(0, 0);
      }

      if (this.page > (numLinks / 2))
      {
         this.setStartIndex(this.page - (numLinks / 2));
      }
      else
      {
         this.setStartIndex(0);
         this.setEndIndex((numLinks > this.pagesCount) ? this.pagesCount : numLinks); 
      }

      if (this.page + (numLinks / 2) >= this.pagesCount)
      {
         this.setEndIndex(this.pagesCount);
         this.setStartIndex((this.pagesCount - numLinks) < 0 ? 0 : this.pagesCount - numLinks);
      }
      else
      {
         if (this.page < (numLinks / 2))
         {
            this.setEndIndex((numLinks > this.pagesCount) ? this.pagesCount : numLinks);
         }
         else
         {
            this.setEndIndex(this.page + (numLinks / 2));
         }

      }
      this.setBoundedIndexes(this.startIndex, this.endIndex);
   }

   public void setBoundedIndexes(int startIndex, int endIndex)
   {
      this.indexes = new ArrayList(endIndex - startIndex);
      for (int i = startIndex; i <= endIndex; i++)
      {
         this.indexes.add(new Integer(i));
      }
   }

   public List<Integer> getIndexes()
   {
      return indexes;
   }

   public void setIndexes(List<Integer> indexes)
   {
      this.indexes = indexes;
   }

   public int getRecordsCount()
   {
      return recordsCount;
   }

   public void setRecordsCount(int recordsCount)
   {
      this.recordsCount = recordsCount;
   }

   public int getStartIndex()
   {
      return startIndex;
   }

   public void setStartIndex(int startIndex)
   {
      this.startIndex = startIndex;
   }

   public int getEndIndex()
   {
      return endIndex;
   }

   public void setEndIndex(int endIndex)
   {
      this.endIndex = endIndex;
   }
}
