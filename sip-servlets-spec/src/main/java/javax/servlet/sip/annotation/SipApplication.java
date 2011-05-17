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

package javax.servlet.sip.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The @SipApplication annotation is used to create an application level annotation for a collection of SipServlets 
 *
 * @since 1.1
 */
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SipApplication {
	String description() default "";
	
	String displayName() default "";
	
	boolean distributable() default false;
	
	String largeIcon() default "";
	
	String smallIcon() default "";
	
	String mainServlet() default "";
	
	String name() default "";
	
	int proxyTimeout() default 180;
	
	int sessionTimeout() default 3;
}
