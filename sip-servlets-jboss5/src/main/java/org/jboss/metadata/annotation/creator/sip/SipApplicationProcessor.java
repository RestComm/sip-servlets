package org.jboss.metadata.annotation.creator.sip;

import java.lang.annotation.Annotation;
import java.util.Collection;

import org.jboss.metadata.annotation.creator.Processor;
import org.jboss.metadata.sip.spec.SipMetaData;

public class SipApplicationProcessor implements
		Processor<SipMetaData, Class<?>> {

	public Collection<Class<? extends Annotation>> getAnnotationTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	public void process(SipMetaData arg0, Class<?> arg1) {
		// TODO Auto-generated method stub

	}

}
