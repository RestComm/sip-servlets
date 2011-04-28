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
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * The @SipApplicationKey annotation is used when the application wants to associate 
 * the incoming request (and SipSession) with a certain SipApplicationSession
 * 
 *  The method annotated with the @SipApplicationKey annotation MUST have the
 * following restrictions:
 * <ol>
 * 	<li> It MUST be public and static
 *  <li> It MUST return a String
 *  <li> It MUST have a single argument of type SipServletRequest
 *  <li> It MUST not modify the SipServletRequest passed in
 * </ol> 
 * If the annotated method signature does not comply with the first three rules,
 * deployment of such an application MUST fail.
 *
 * @since 1.1
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface SipApplicationKey {
	String applicationName() default "";
}
