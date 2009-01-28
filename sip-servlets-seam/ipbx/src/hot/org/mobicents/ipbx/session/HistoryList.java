package org.mobicents.ipbx.session;

import java.util.LinkedList;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import org.mobicents.ipbx.entity.History;

@Name("historyList")
public class HistoryList extends EntityQuery<History>
{
    public HistoryList()
    {
        setEjbql("select history from History history");
        LinkedList restrictions = new LinkedList();
        restrictions.add("history.user = #{user}");
        setRestrictionExpressionStrings(restrictions);
    }
}