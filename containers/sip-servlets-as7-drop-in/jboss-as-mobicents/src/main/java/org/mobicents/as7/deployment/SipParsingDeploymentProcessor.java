/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Inc., and individual contributors as indicated
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
package org.mobicents.as7.deployment;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jboss.as.ee.structure.DeploymentType;
import org.jboss.as.ee.structure.DeploymentTypeMarker;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.metadata.parser.util.MetaDataElementParser;
import org.jboss.vfs.VirtualFile;
import org.mobicents.metadata.sip.parser.SipMetaDataParser;
import org.mobicents.metadata.sip.spec.SipMetaData;

/**
 * @author Jean-Frederic Clere
 * @author Thomas.Diesler@jboss.com
 * @author josemrecio@gmail.com
 */
public class SipParsingDeploymentProcessor implements DeploymentUnitProcessor {

    protected static final String SIP_XML = "WEB-INF/sip.xml";
    private final boolean schemaValidation = true;

    public SipParsingDeploymentProcessor() {
        // TODO: josemrecio - is this needed?
//        String property = SecurityActions.getSystemProperty(XMLSchemaValidator.PROPERTY_SCHEMA_VALIDATION, "false");
//        this.schemaValidation = Boolean.parseBoolean(property);
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        final VirtualFile rootFile = deploymentUnit.getAttachment(Attachments.DEPLOYMENT_ROOT).getRoot();
        // TODO: I think this does not apply to sip.xml
        // final VirtualFile alternateDescriptor =
        // deploymentRoot.getAttachment(org.jboss.as.ee.structure.Attachments.ALTERNATE_SIP_DEPLOYMENT_DESCRIPTOR);
        // // Locate the descriptor
        // final VirtualFile sipXml;
        // if (alternateDescriptor != null) {
        // sipXml = alternateDescriptor;
        // } else {
        // sipXml = deploymentRoot.getRoot().getChild(SIP_XML);
        // }

        VirtualFile sipXml = null;
        /*if (DeploymentTypeMarker.isType(DeploymentType.EAR, deploymentUnit)) {
            List<VirtualFile> children = rootFile.getChildren();
            for (VirtualFile child: children) {
                if (child.getChild(SIP_XML).exists()) {
                    sipXml = child.getChild(SIP_XML);
                }
            }
        }
        else */if (DeploymentTypeMarker.isType(DeploymentType.WAR, deploymentUnit)) {
            sipXml = rootFile.getChild(SIP_XML);
        }
        if ((sipXml == null) || !sipXml.exists()) {
            return;
        }

        InputStream is = null;
        try {
            is = sipXml.openStream();
            final XMLInputFactory inputFactory = XMLInputFactory.newInstance();

            MetaDataElementParser.DTDInfo dtdInfo = new MetaDataElementParser.DTDInfo();
            inputFactory.setXMLResolver(dtdInfo);
            final XMLStreamReader xmlReader = inputFactory.createXMLStreamReader(is);
            SipMetaData sipMetaData = SipMetaDataParser.parse(xmlReader, dtdInfo);
            deploymentUnit.putAttachment(SipMetaData.ATTACHMENT_KEY, sipMetaData);
        } catch (XMLStreamException e) {
            throw new DeploymentUnitProcessingException("Failed to parse " + sipXml + " at ["
                    + e.getLocation().getLineNumber() + "," + e.getLocation().getColumnNumber() + "]");
        } catch (IOException e) {
            throw new DeploymentUnitProcessingException("Failed to parse " + sipXml, e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        // TODO Auto-generated method stub

    }
}
