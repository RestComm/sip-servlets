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
