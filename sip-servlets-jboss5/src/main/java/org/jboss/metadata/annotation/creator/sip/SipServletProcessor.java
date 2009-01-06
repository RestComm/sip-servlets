package org.jboss.metadata.annotation.creator.sip;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;

import javax.servlet.sip.annotation.SipServlet;

import org.jboss.metadata.annotation.creator.AbstractComponentProcessor;
import org.jboss.metadata.annotation.creator.Processor;
import org.jboss.metadata.annotation.creator.ProcessorUtils;
import org.jboss.metadata.annotation.finder.AnnotationFinder;
import org.jboss.metadata.sip.spec.SipMetaData;
import org.jboss.metadata.web.spec.ServletMetaData;
import org.jboss.metadata.web.spec.ServletsMetaData;

public class SipServletProcessor extends
		AbstractComponentProcessor<ServletMetaData> implements
		Processor<ServletMetaData, Class<?>> {

	public SipServletProcessor(AnnotationFinder<AnnotatedElement> finder) {
		super(finder);
	}
	
	public void process(SipMetaData sipMetaData, Class<?> beanClass) {
		ServletMetaData beanMetaData = create(beanClass);
		if(beanMetaData == null)
	         return; 
		ServletsMetaData servletsMetaData = sipMetaData.getSipServlets(); 
		if(servletsMetaData == null) {
			servletsMetaData = new ServletsMetaData();
			sipMetaData.setSipServlets(servletsMetaData);			
		}
		servletsMetaData.add(beanMetaData);
	}
	
	public ServletMetaData create(Class<?> beanClass) {
		SipServlet annotation = finder.getAnnotation(beanClass, SipServlet.class);
		if (annotation == null)
			return null;

		ServletMetaData beanMetaData = create(beanClass, annotation);
		return beanMetaData;
	}

	protected ServletMetaData create(Class<?> beanClass,
			SipServlet annotation) {
		return create(beanClass, annotation.applicationName(), annotation.description(),
				annotation.loadOnStartup());
	}

	private ServletMetaData create(Class<?> beanClass, String applicationName,
			String description, int loadOnStartup) {
		
		ServletMetaData servletMetaData = new ServletMetaData();
		servletMetaData.setServletName(applicationName);
		servletMetaData.setServletClass(beanClass.getClass().getName());
		servletMetaData.setLoadOnStartup(loadOnStartup);
		
		return servletMetaData;
	}

	public Collection<Class<? extends Annotation>> getAnnotationTypes() {
		return ProcessorUtils.createAnnotationSet(SipServlet.class);
	}

}
