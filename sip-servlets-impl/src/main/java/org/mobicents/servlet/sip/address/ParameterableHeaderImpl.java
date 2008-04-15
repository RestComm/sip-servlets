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
