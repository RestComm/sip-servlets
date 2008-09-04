package org.mobicents.servlet.sip.address;

import java.text.ParseException;

import gov.nist.core.NameValueList;
import gov.nist.javax.sip.header.AddressParametersHeader;

import javax.servlet.sip.URI;
import javax.sip.address.Address;

public class AddressReadOnlyImpl extends AddressImpl {

	public AddressReadOnlyImpl() {
		super();
	}

	public AddressReadOnlyImpl(Address address, NameValueList parameters,
			boolean isModifiable) {
		super(address, parameters, isModifiable);
	}

	public AddressReadOnlyImpl(AddressParametersHeader header)
			throws ParseException {
		super(header);
	}

	@Override
	public void setAddress(Address address) {
		throw new IllegalStateException("Can not modify read only address.");
	}

	@Override
	public void setDisplayName(String name) {
		throw new IllegalStateException("Can not modify read only address.");
	}

	@Override
	public void setExpires(int seconds) throws IllegalArgumentException {
		throw new IllegalStateException("Can not modify read only address.");
	}

	@Override
	public void setParameter(String name, String value) {
		throw new IllegalStateException("Can not modify read only address.");
	}

	@Override
	public void setQ(float q) {
		throw new IllegalStateException("Can not modify read only address.");
	}

	@Override
	public void setURI(URI uri) {
		throw new IllegalStateException("Can not modify read only address.");
	}

	@Override
	public void setValue(String value) {
		throw new IllegalStateException("Can not modify read only address.");
	}

	@Override
	public void setParameters(NameValueList parameters) {
		throw new IllegalStateException("Can not modify read only address.");
	}

}
