/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javax.servlet.sip.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The @SipApplicationKey annotation is used when the application wants to associate 
 * the incoming request (and SipSession) with a certain SipApplicationSession
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
	
	int sessionTimeout() default 180;
}
