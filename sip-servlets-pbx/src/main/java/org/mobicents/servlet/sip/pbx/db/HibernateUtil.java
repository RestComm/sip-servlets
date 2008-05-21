/*
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
package org.mobicents.servlet.sip.pbx.db;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.mobicents.servlet.sip.pbx.registrar.RegistrarService;

/**
 * @author Thomas Leseney
 */
public class HibernateUtil {
	
	private static Log logger = LogFactory.getLog(RegistrarService.class);
	
	static SessionFactory sessionFactory;

	static {
        try {
           sessionFactory = new Configuration().configure().buildSessionFactory();
        } catch (Throwable e) {
            logger.error("Hibernate init failed", e);
            throw new ExceptionInInitializerError(e);
        }
    }
	
	public static SessionFactory getSessionFactory() {
	      return sessionFactory;
	}
}
