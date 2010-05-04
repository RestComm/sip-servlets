//$Id: FullTextSearch.java,v 1.1 2007/11/19 14:16:15 abhayani Exp $
package org.jboss.mobicents.seam.actions;

/**
 * @author Emmanuel Bernard
 */
public interface FullTextSearch
{
   public String getSearchQuery();

   public void setSearchQuery(String searchQuery);

   public int getNumberOfResults();

   public void nextPage();

   public void prevPage();

   public boolean isLastPage();

   public boolean isFirstPage();

   public String doSearch();

   public void selectFromRequest();

   public void addToCart();

   public void addAllToCart();

   public int getPageSize();

   public void setPageSize(int pageSize);
  
   public void reset();

   public void destroy();
}
