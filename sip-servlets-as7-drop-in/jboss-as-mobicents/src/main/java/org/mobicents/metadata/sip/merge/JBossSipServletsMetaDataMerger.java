/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.mobicents.metadata.sip.merge;

import org.jboss.metadata.merge.web.jboss.JBossServletMetaDataMerger;
import org.jboss.metadata.web.jboss.JBossServletMetaData;
import org.jboss.metadata.web.spec.ServletMetaData;
import org.mobicents.metadata.sip.jboss.JBossSipServletsMetaData;
import org.mobicents.metadata.sip.spec.SipServletsMetaData;

/**
 * jboss-web/servlet collection
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 66673 $
 */
public class JBossSipServletsMetaDataMerger {
    public static JBossSipServletsMetaData merge(JBossSipServletsMetaData override, SipServletsMetaData original) {
        JBossSipServletsMetaData merged = new JBossSipServletsMetaData();
        if (override == null && original == null)
            return merged;

        if (original != null) {
            for (ServletMetaData smd : original) {
                String key = smd.getKey();
                if (override != null && override.containsKey(key)) {
                    JBossServletMetaData overrideSMD = override.get(key);
                    JBossServletMetaData jbs = new JBossServletMetaData();
                    JBossServletMetaDataMerger.merge(jbs, overrideSMD, smd);
                    merged.add(jbs);
                } else {
                    JBossServletMetaData jbs = new JBossServletMetaData();
                    JBossServletMetaDataMerger.merge(jbs, null, smd);
                    merged.add(jbs);
                }
            }
        }

        // Process the remaining overrides
        if (override != null) {
            for (JBossServletMetaData jbs : override) {
                String key = jbs.getKey();
                if (merged.containsKey(key))
                    continue;
                merged.add(jbs);
            }
        }

        return merged;
    }
}
