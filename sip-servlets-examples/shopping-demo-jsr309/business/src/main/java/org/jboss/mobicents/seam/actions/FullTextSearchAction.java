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

//$Id: FullTextSearchAction.java,v 1.1 2007/11/19 14:16:15 abhayani Exp $
package org.jboss.mobicents.seam.actions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.jboss.mobicents.seam.model.Product;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.annotations.web.RequestParameter;

/**
 * Hibernate Search version of the store querying mechanism
 * @author Emmanuel Bernard
 */
@Stateful
@Name("search")
public class FullTextSearchAction
    implements FullTextSearch,
               Serializable
{
    static final long serialVersionUID = -6536629890251170098L;
    
    @In(create = true)
    ShoppingCart cart;
    
    @PersistenceContext
    EntityManager em;

    @RequestParameter
    Long id;

    int pageSize = 15;
    int currentPage = 0;
    boolean hasMore = false;
    int numberOfResults;
    
    String searchQuery;

    @DataModel
    List<Product> searchResults;

    @DataModelSelection
    Product selectedProduct;

    @Out(required = false)
    Product dvd;

    @Out(scope = ScopeType.CONVERSATION, required = false)
    Map<Product, Boolean> searchSelections;


    public String getSearchQuery() {
        return searchQuery;
    }
    
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
    
    
    public int getNumberOfResults() {
        return numberOfResults;
    }
    
    @Begin(join = true)
    public String doSearch() {
        currentPage = 0;
        updateResults();
        
        return "browse";
    }
    
    public void nextPage() {
        if (!isLastPage()) {
            currentPage++;
            updateResults();
        }
    }

    public void prevPage() {
        if (!isFirstPage()) {
            currentPage--;
            updateResults();
        }
    }
    
    @Begin(join = true)
    public void selectFromRequest() {
        if (id != null)  {
            dvd = em.find(Product.class, id);
        } else if (selectedProduct != null) {
            dvd = selectedProduct;
        }
    }

    public boolean isLastPage() {
        return ( searchResults != null ) && !hasMore;
    }

    public boolean isFirstPage() {
        return ( searchResults != null ) && ( currentPage == 0 );
    }

    @SuppressWarnings("unchecked")
    private void updateResults() {
        FullTextQuery query;
        try {
            query = searchQuery(searchQuery);
        } catch (ParseException pe) { 
            return; 
        }
      
        List<Product> items = query
            .setMaxResults(pageSize + 1)
            .setFirstResult(pageSize * currentPage)
            .getResultList();
        numberOfResults = query.getResultSize();
        
        if (items.size() > pageSize) {
            searchResults = new ArrayList(items.subList(0, pageSize));
            hasMore = true;
        } else {
            searchResults = items;
            hasMore = false;
        }

        searchSelections = new HashMap<Product, Boolean>();
    }

    private FullTextQuery searchQuery(String searchQuery) throws ParseException
    {
        Map<String,Float> boostPerField = new HashMap<String,Float>();
        boostPerField.put("title", 4f);
        boostPerField.put("description", 2f);
        boostPerField.put("actors.name", 2f);
        boostPerField.put("categories.name", 0.5f);

        String[] productFields = {"title", "description", "actors.name", "categories.name"};
        QueryParser parser = new MultiFieldQueryParser(productFields, new StandardAnalyzer(), boostPerField);
        parser.setAllowLeadingWildcard(true);
        org.apache.lucene.search.Query luceneQuery;
        luceneQuery = parser.parse(searchQuery);
        return ( (FullTextEntityManager) em ).createFullTextQuery(luceneQuery, Product.class);
    }
    
    /**
     * Add the selected DVD to the cart
     */
    public void addToCart()
    {
        cart.addProduct(dvd, 1);
    }
    
    /**
     * Add many items to cart
     */
    public void addAllToCart()
    {
        for (Product item : searchResults) {
            Boolean selected = searchSelections.get(item);
            if (selected != null && selected) {
                searchSelections.put(item, false);
                cart.addProduct(item, 1);
            }
        }
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    
    @End
    public void reset() { }

    @Destroy
    @Remove
    public void destroy() { }
}
