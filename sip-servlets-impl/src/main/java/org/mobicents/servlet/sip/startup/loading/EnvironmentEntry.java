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

package org.mobicents.servlet.sip.startup.loading;


public class EnvironmentEntry {
	public String description;
	public String name;
	public String value;
	public String type;
	public static final String BOOLEAN = "java.lang.Boolean";
	public static final String BYTE = "java.lang.Byte";
	public static final String CHARACTER = "java.lang.Character";
	public static final String STRING = "java.lang.String";
	public static final String SHORT = "java.lang.Short";
	public static final String INTEGER = "java.lang.Integer";
	public static final String LONG = "java.lang.Long";
	public static final String DOUBLE = "java.lang.Double";
	public static final String FLOAT = "java.lang.Float";
}
