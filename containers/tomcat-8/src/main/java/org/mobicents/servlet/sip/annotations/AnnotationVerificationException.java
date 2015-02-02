/*
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
package org.mobicents.servlet.sip.annotations;

/**
 * Thrown when an annotation doesn't respect the restrictions it shoud follow
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class AnnotationVerificationException extends Exception {

	/**
	 * {@inheritDoc}
	 */
	public AnnotationVerificationException() {
	}

	/**
	 * {@inheritDoc}
	 */
	public AnnotationVerificationException(String message) {
		super(message);
	}

	/**
	 * {@inheritDoc}
	 */
	public AnnotationVerificationException(Throwable cause) {
		super(cause);
	}

	/**
	 * {@inheritDoc}
	 */
	public AnnotationVerificationException(String message, Throwable cause) {
		super(message, cause);
	}

}
