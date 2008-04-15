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
package org.mobicents.servlet.sip.address;

import java.util.Map;

import gov.nist.core.NameValueList;

/**
 * This class is impl of Parameterable object returned 
 * @author baranowb
 *
 */
public class ParameterableHeaderImpl extends ParameterableImpl {

	
	
	protected String value=null;
	
	
	public ParameterableHeaderImpl() {
		super();

	}

	public ParameterableHeaderImpl(String value,Map params) {
		//General form of parametrable header
		
		super(params);
		this.value=value;
	}

	@Override
	public Object clone() {
		
		ParameterableHeaderImpl cloned=new ParameterableHeaderImpl();
		cloned.parameters=(NameValueList) super.parameters.clone();
		cloned.value=new String(this.value);
		return cloned;
		
	}

	public String getValue() {
		
		return this.value;
	}

	public void setValue(String value) {

		this.value=value;

	}

}
