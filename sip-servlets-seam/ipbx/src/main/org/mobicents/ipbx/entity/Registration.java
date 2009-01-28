package org.mobicents.ipbx.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

@Entity
@Table(name="REGISTRATIONS")
public class Registration implements Serializable {
	private String uri;
	private long id;
	private User user;
	private CallState callState;
	private boolean selected;
	private Integer version;

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
}
