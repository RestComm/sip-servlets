package org.mobicents.as7;

import java.util.Locale;

import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.dmr.ModelNode;

/**
 * Common web description providers.
 *
 * @author Emanuel Muckenhuber
 */
class SipSubsystemDescriptionProviders {

    public static final DescriptionProvider SUBSYSTEM = new DescriptionProvider() {
        @Override
        public ModelNode getModelDescription(Locale locale) {
            return SipSubsystemDescriptions.getSubsystemDescription(locale);
        }
    };

    public static final DescriptionProvider SUBSYSTEM_REMOVE = new DescriptionProvider() {
        @Override
        public ModelNode getModelDescription(Locale locale) {
            return SipSubsystemDescriptions.getSubsystemRemoveDescription(locale);
        }
    };

    public static final DescriptionProvider CONNECTOR = new DescriptionProvider() {
        @Override
        public ModelNode getModelDescription(Locale locale) {
            return SipSubsystemDescriptions.getConnectorDescription(locale);
        }
    };

    public static final DescriptionProvider DEPLOYMENT = new DescriptionProvider() {
        @Override
        public ModelNode getModelDescription(Locale locale) {
            return SipSubsystemDescriptions.getDeploymentRuntimeDescription(locale);
        }
    };

    public static final DescriptionProvider SERVLET = new DescriptionProvider() {
        @Override
        public ModelNode getModelDescription(Locale locale) {
            return SipSubsystemDescriptions.getDeploymentServletDescription(locale);
        }
    };

}
