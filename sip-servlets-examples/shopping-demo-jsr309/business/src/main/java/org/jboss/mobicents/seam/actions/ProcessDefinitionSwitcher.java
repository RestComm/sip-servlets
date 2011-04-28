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

package org.jboss.mobicents.seam.actions;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.bpm.Jbpm;
import org.jbpm.JbpmContext;

/**
 * Switches JBPM process definitions dynamically
 */
@Name("processDefinitionSwitcher")
@Scope(ScopeType.APPLICATION)
public class ProcessDefinitionSwitcher {
	static final String[] ORDER_DEFS = { "ordermanagement3.jpdl.xml",
			"ordermanagement2.jpdl.xml", "ordermanagement1.jpdl.xml" };

	@In(value = "org.jboss.seam.bpm.jbpm")
	private Jbpm jbpm;

	@In
	private JbpmContext jbpmContext;

	public List<SelectItem> getProcessDefinitions() {
		List<SelectItem> result = new ArrayList<SelectItem>();
		for (String def : ORDER_DEFS) {
			result.add(new SelectItem(def, def.substring(0, def.length() - 9)));
		}
		return result;
	}

	private String currentProcessDefinition;

	public String getCurrentProcessDefinition() {
		return currentProcessDefinition;
	}

	public void setCurrentProcessDefinition(String def) {
		currentProcessDefinition = def;
	}

	public String switchProcess() {
		jbpmContext.deployProcessDefinition(jbpm
				.getProcessDefinitionFromResource(currentProcessDefinition));
		return null;
	}

}
