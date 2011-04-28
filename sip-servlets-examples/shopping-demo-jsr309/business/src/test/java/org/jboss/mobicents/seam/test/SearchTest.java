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

package org.jboss.mobicents.seam.test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import javax.faces.model.ListDataModel;

import org.jboss.mobicents.seam.actions.FullTextSearch;
import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.Test;

public class SearchTest 
    extends SeamTest
{   
    @Test
    public void testNoParamSearch() 
        throws Exception
    {
        
        new FacesRequest() {
           FullTextSearch search;
            @Override
            protected void updateModelValues()
            {
                search = (FullTextSearch) getInstance("search");
                search.setSearchQuery("king");
            }
            @Override
            protected void invokeApplication()
            {
                String outcome = search.doSearch();
                assertEquals("search outcome", "browse", outcome);
            }
            @Override
            protected void renderResponse()
            {
                ListDataModel model = (ListDataModel) lookup("searchResults");
                assertEquals("page size", 4, model.getRowCount());
                assertTrue("in conversation", isLongRunningConversation());
            }               
        }.run();
    }
    
}
