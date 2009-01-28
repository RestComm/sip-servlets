package org.mobicents.ipbx.session;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.Unwrap;

@Name("entityManager2")
@Scope(ScopeType.SESSION)
@Startup
public class EntityManagerLoader {
	@In EntityManagerFactory ipbxEntityManagerFactory;
	
	@Unwrap
	public EntityManager getEntityManager() {
		return ipbxEntityManagerFactory.createEntityManager();
	}

}
