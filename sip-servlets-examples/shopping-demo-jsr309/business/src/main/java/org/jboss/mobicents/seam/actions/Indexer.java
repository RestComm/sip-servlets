//$Id: Indexer.java,v 1.1 2007/11/19 14:16:15 abhayani Exp $
package org.jboss.mobicents.seam.actions;

import java.util.Date;

/**
 * @author Emmanuel Bernard
 */
public interface Indexer
{
   Date getLastIndexingTime();
   void index();
   void stop();
}
