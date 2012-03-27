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

package org.jboss.deployment;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.List;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.metadata.annotation.creator.sip.Sip11MetaDataCreator;
import org.jboss.metadata.annotation.finder.AnnotationFinder;
import org.jboss.metadata.annotation.finder.DefaultAnnotationFinder;
import org.jboss.metadata.sip.jboss.JBossSip11MetaData;
import org.jboss.metadata.sip.spec.Sip11MetaData;
import org.jboss.metadata.sip.spec.SipMetaData;
import org.jboss.virtual.VirtualFile;

/**
 * A POST_CLASSLOADER deployer which generates metadata from sip annotations.
 * Overriding the Optimized AnnotationMetaDataDeployer from JBoss 5
 * 
 * @author jean.deruelle@gmail.com
 * 
 */
public class ConvergedSipAnnotationMetaDataDeployer extends
		OptAnnotationMetaDataDeployer {

	public static final String SIP_ANNOTATED_ATTACHMENT_NAME = "sip.annotated."
			+ SipMetaData.class.getName();

	/**
	 * 
	 */
	public ConvergedSipAnnotationMetaDataDeployer() {
		super();
		addInput(SipMetaData.class);
		addOutput(SIP_ANNOTATED_ATTACHMENT_NAME);
	}

	@Override
	protected void deploy(VFSDeploymentUnit unit) throws DeploymentException {
		boolean isComplete = this.isMetaDataCompleteIsDefault();

		SipMetaData sipMetaData = unit.getAttachment(SipMetaData.class);
		if (sipMetaData != null && sipMetaData instanceof Sip11MetaData) {
			isComplete |= ((Sip11MetaData) sipMetaData).isMetadataComplete();
		} else if (sipMetaData != null && sipMetaData instanceof JBossSip11MetaData) {
			isComplete |= ((JBossSip11MetaData) sipMetaData).isMetadataComplete();
		} else if (sipMetaData != null) {
			// Any sip.xml 1.0 is metadata complete
			isComplete = true;
		}

		VirtualFile root = unit.getRoot();
		boolean isLeaf = true;
		try {
			isLeaf = root.isLeaf();
		} catch (IOException ignore) {
		}
		if (isLeaf == true)
			return;

		List<VirtualFile> classpath = unit.getClassPath();
		if (classpath == null || classpath.isEmpty())
			return;

		if (!isComplete) {
			try {
				processSipMetaData(unit, sipMetaData, classpath);
			} catch (Exception e) {
				throw DeploymentException.rethrowAsDeploymentException(
						"Cannot process metadata", e);
			}
		}

		super.deploy(unit);
	}
	
	protected void processSipMetaData(VFSDeploymentUnit unit,
			SipMetaData sipMetaData, List<VirtualFile> classpath) throws IOException {
		String mainClassName = getMainClassName(unit);
		Collection<Class<?>> classes = getClasses(unit, mainClassName,
				classpath);
		if (classes.size() > 0) {
			AnnotationFinder<AnnotatedElement> finder = new DefaultAnnotationFinder<AnnotatedElement>();
//			if (sipMetaData != null)
				processSipMetaData(unit, finder, classes);			
		}

	}

	/**
	 * Process sip meta data.
	 *
	 * @param unit the deployment unit
	 * @param finder the finder
	 * @param classes 
	 */
	protected void processSipMetaData(VFSDeploymentUnit unit,
			AnnotationFinder<AnnotatedElement> finder,
			Collection<Class<?>> classes) {
		
		Sip11MetaDataCreator creator = new Sip11MetaDataCreator(finder);
	    SipMetaData annotationMetaData = creator.create(classes);
	    if(annotationMetaData != null)
	         unit.addAttachment(SIP_ANNOTATED_ATTACHMENT_NAME, annotationMetaData, SipMetaData.class);

	}
}
