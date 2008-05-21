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

import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.hibernate.Query;
import org.hibernate.Session;
import org.mobicents.servlet.sip.pbx.location.Binding;
import org.mobicents.servlet.sip.pbx.location.LocationService;

/**
 * @author Thomas Leseney
 */
public class DbLocationService implements LocationService {

	private Timer timer;
	
	public DbLocationService() {
	}
	
	public void start() throws Exception {
		HibernateUtil.getSessionFactory();
		timer = new Timer();
		timer.schedule(new Scavenger(), 5000, 5000);
	}
	
	public void stop() throws Exception {
		timer.cancel();
		HibernateUtil.getSessionFactory().close();
	}
	
	private Session getSession() {
		return HibernateUtil.getSessionFactory().getCurrentSession();
	}
	
	public Binding createBinding(String aor, String contact) {
		return new Binding(aor, contact);
	}
	
	public List<Binding> getBindings(String aor) {
		return getSession().createQuery("FROM Binding b where b.aor=:aor").setParameter("aor", aor).list();
	}
	
	public List<Binding> getAllBindings() {
		return getSession().createQuery("FROM Binding b").list();
	}
	
	public void addBinding(Binding binding) {
		getSession().save(binding);
	}
	
	public void updateBinding(Binding binding) { 
	}
	
	public void removeBinding(Binding binding) {
		getSession().delete(binding);
	}
	
	public void removeExpiredBindings() {
		Session session = getSession();
		Query query = session.createQuery("FROM Binding WHERE expirationTime < :now");
		query.setParameter("now", System.currentTimeMillis());
		Iterator<Binding> it = query.iterate();
		while (it.hasNext()) {
			Binding binding = (Binding) it.next();
			session.delete(binding);
		}
	}
	
	public void beginTransaction() {
		getSession().beginTransaction();
	}
	
	public void commitTransaction() {
		getSession().getTransaction().commit();
	}

	public void rollbackTransaction() {
		getSession().getTransaction().rollback();
	}
	
	class Scavenger extends TimerTask {
		public void run() {
			try {
				beginTransaction();
				removeExpiredBindings();
			} finally {
				commitTransaction();
			}
		}
	}
}
