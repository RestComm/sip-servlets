package org.mobicents.ipbx.entity;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.jboss.seam.annotations.Name;

@Entity
@Name("registration")
@Table(name="REGISTRATIONS")
/**
 * This class to a given registration.
 * The registration is added in the pbx UI by the admin or user. This registration can be either a uri whose the hostname is a ipaddress:port string
 * or a hostname of the form mobicents.org by example. In the former case, it will be used directly, in the latter case, a set of registrations
 * location will be maintained and populated by the RegistrarService.
 * Example for the latter case, for a REGISTER with a From: sip:jean@mobicents.org header and a Contact: sip:jean@127.0.0.1:5060 headers
 * The registration is the URI of the FROM header and the RegistrationLocation is the URI of the Contact Header without the parameters
 * 
 * @author jean.deruelle@gmail.com
 * @author vralev
 */
public class Registration implements Serializable {
	private String uri;
	private boolean isIpAddressURI;
	private long id;
	private User user;
	private CallState callState;
	private boolean selected;
	private Integer version;
	private Set<Binding> bindings;
	
	@Version
	public Integer getVersion() {
		return version;
	}

	private void setVersion(Integer version) {
		this.version = version;
	}

	@ManyToOne
	@JoinColumn(name="USERID") 
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Id @GeneratedValue(strategy=GenerationType.SEQUENCE)
	@Column(name="REGISTRATIONID")
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	@Transient
	public CallState getCallState() {
		return callState;
	}

	public void setCallState(CallState callState) {
		this.callState = callState;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * @return the isIpAddressURI
	 */
	@Transient
	public boolean isIpAddressURI() {
		return isIpAddressURI;
	}

	/**
	 * 
	 */
	public void addBinding(Binding binding) {
		this.bindings.add(binding);
	}

	/**
	 * 
	 */
	public void removeBinding(Binding binding) {
		this.bindings.remove(binding);
	}
	
	public void updateBinding(Binding binding) {
		this.bindings.remove(binding);
		this.bindings.add(binding);
	} 
	
	@OneToMany(mappedBy="registration",cascade={CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH},fetch=FetchType.EAGER)
	public Set<Binding> getBindings() {
		return this.bindings;
	}
	
	public void setBindings(Set<Binding> bindings) {
		this.bindings = bindings;
	}
	
//	@Transient
//	public String[] getCallableUris() {
//		LinkedList<String> uris = new LinkedList<String>();
//		uris.add(getUri());
//		if(getBindings() != null) {
//			Iterator<Binding> bindings = getBindings().iterator();
//			while(bindings.hasNext()) {
//				Binding binding = bindings.next();
//				uris.add(binding.getContactAddress());
//			}
//		}
//		return uris.toArray(new String[]{});
//	}
//	
//	// This method doesn't return this.uri in the list
//	@Transient
//	public Binding[] getCallableBindings() {
//		LinkedList<Binding> uris = new LinkedList<Binding>();
//		if(getBindings() != null) {
//			Iterator<Binding> bindings = getBindings().iterator();
//			while(bindings.hasNext()) {
//				Binding binding = bindings.next();
//				uris.add(binding);
//			}
//		}
//		return uris.toArray(new Binding[]{});
//	}
}
