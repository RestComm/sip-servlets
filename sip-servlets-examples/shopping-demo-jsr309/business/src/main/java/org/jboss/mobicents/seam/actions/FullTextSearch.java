/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

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
