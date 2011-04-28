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

package org.jboss.sip.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployment.AnnotationMetaDataDeployer;
import org.jboss.deployment.ConvergedSipAnnotationMetaDataDeployer;
import org.jboss.metadata.ear.jboss.JBossAppMetaData;
import org.jboss.metadata.javaee.spec.SecurityRolesMetaData;
import org.jboss.metadata.sip.jboss.JBossConvergedSipMetaData;
import org.jboss.metadata.sip.spec.Sip11MetaData;
import org.jboss.metadata.sip.spec.SipAnnotationMergedView;
import org.jboss.metadata.sip.spec.SipMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.AnnotationMergedView;
import org.jboss.metadata.web.spec.Web25MetaData;
import org.jboss.metadata.web.spec.WebMetaData;
import org.jboss.web.deployers.MergedJBossWebMetaDataDeployer;

/**
 * A deployer that extends MergedJBossWebMetaDataDeployer to provide support for pure sip applications or converged http/sip applications
 * 
 * A deployer that merges web annotation metadata, sip annotation metadata, xml web metadata, xml sip metadata and jboss metadata
 * into a merged JBossConvergedSipMetaData. It also incorporates ear level overrides from
 * the top level JBossAppMetaData attachment.
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class MergedJBossConvergedSipMetaDataDeployer extends
		MergedJBossWebMetaDataDeployer {
	
	public static final String CONVERGED_MERGED_ATTACHMENT_NAME = "merged."+JBossConvergedSipMetaData.class.getName();

	   /**
	    * Create a new MergedJBossConvergedSipMetaDataDeployer.
	    */
	   public MergedJBossConvergedSipMetaDataDeployer()
	   {
	      setStage(DeploymentStages.POST_CLASSLOADER);
	      // web.xml metadata
	      addInput(WebMetaData.class);
	      // sip.xml metadata
	      addInput(SipMetaData.class);
	      // jboss.xml metadata
	      addInput(JBossConvergedSipMetaData.class);
	      // annotated metadata view
	      addInput(AnnotationMetaDataDeployer.WEB_ANNOTATED_ATTACHMENT_NAME);
	      // annotated sip metadata view
	      addInput(ConvergedSipAnnotationMetaDataDeployer.SIP_ANNOTATED_ATTACHMENT_NAME);
	      // Output is the merge JBossConvergedSipMetaData view
	      setOutput(JBossConvergedSipMetaData.class);
	      // 
	      addOutput(CONVERGED_MERGED_ATTACHMENT_NAME);
	   }

	   public void deploy(DeploymentUnit unit) throws DeploymentException
	   {
	      WebMetaData specMetaData = unit.getAttachment(WebMetaData.class);
	      SipMetaData specSipMetaData = unit.getAttachment(SipMetaData.class);
	      String sipKey = ConvergedSipAnnotationMetaDataDeployer.SIP_ANNOTATED_ATTACHMENT_NAME;
	      Sip11MetaData sipAnnotatedMetaData = unit.getAttachment(sipKey, Sip11MetaData.class);
	      JBossConvergedSipMetaData metaData = unit.getAttachment(JBossConvergedSipMetaData.class);
	      if(specMetaData == null && metaData == null && specSipMetaData == null && sipAnnotatedMetaData == null)
	         return;

	      // Check for an annotated view
	      String key = AnnotationMetaDataDeployer.WEB_ANNOTATED_ATTACHMENT_NAME;
	      Web25MetaData annotatedMetaData = unit.getAttachment(key, Web25MetaData.class);
	      if(annotatedMetaData != null)
	      {
	         if(specMetaData != null)
	         {
	            Web25MetaData specMerged = new Web25MetaData();
	            // TODO: JBMETA-7
	            AnnotationMergedView.merge(specMerged, specMetaData, annotatedMetaData);
	            specMetaData = specMerged;
	         }
	         else
	            specMetaData = annotatedMetaData;
	      }
	      
	      // Check for a sip annotated view	      
	      if(sipAnnotatedMetaData != null)
	      {
	         if(specSipMetaData != null)
	         {
	            Sip11MetaData specMerged = new Sip11MetaData();
	            // TODO: JBMETA-7
	            SipAnnotationMergedView.merge(specMerged, specSipMetaData, sipAnnotatedMetaData);
	            specSipMetaData = specMerged;
	         }
	         else
	            specSipMetaData = sipAnnotatedMetaData;
	      }

	      // Create a merged view
	      JBossConvergedSipMetaData mergedMetaData = new JBossConvergedSipMetaData();
	      mergedMetaData.merge(metaData, specMetaData);
	      mergedMetaData.merge(metaData, specSipMetaData);
	      // Incorporate any ear level overrides
	      DeploymentUnit topUnit = unit.getTopLevel();
	      if(topUnit != null && topUnit.getAttachment(JBossAppMetaData.class) != null)
	      {
	         JBossAppMetaData earMetaData = topUnit.getAttachment(JBossAppMetaData.class);
	         // Security domain
	         String securityDomain = earMetaData.getSecurityDomain();
	         if(securityDomain != null && mergedMetaData.getSecurityDomain() == null)
	            mergedMetaData.setSecurityDomain(securityDomain);
	         //Security Roles
	         SecurityRolesMetaData earSecurityRolesMetaData = earMetaData.getSecurityRoles();
	         if(earSecurityRolesMetaData != null)
	         {
	            SecurityRolesMetaData mergedSecurityRolesMetaData = mergedMetaData.getSecurityRoles(); 
	            if(mergedSecurityRolesMetaData == null)
	               mergedMetaData.setSecurityRoles(earSecurityRolesMetaData);
	            
	            //perform a merge to rebuild the principalVersusRolesMap
	            if(mergedSecurityRolesMetaData != null )
	            {
	                mergedSecurityRolesMetaData.merge(mergedSecurityRolesMetaData, 
	                     earSecurityRolesMetaData);
	            }
	        }
	      }

	      // Output the merged JBossConvergedSipMetaData
	      unit.getTransientManagedObjects().addAttachment(JBossConvergedSipMetaData.class, mergedMetaData);
	      if(unit.getAttachment(JBossWebMetaData.class) != null) {
		      // Fix for Issue 1398 to overcome web service problems, we put the merged meta data in place of the JBossWebMetaData
		      // so that jboss web service aspects do their modification on the converged
		      unit.getTransientManagedObjects().addAttachment(JBossWebMetaData.class, mergedMetaData);
	      }
	   }
}
