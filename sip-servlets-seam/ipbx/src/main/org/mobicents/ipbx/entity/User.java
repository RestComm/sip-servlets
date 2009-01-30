package org.mobicents.ipbx.entity;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Version;

import javax.persistence.*;
import org.hibernate.validator.Length;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Entity
@Name("user")
@Scope(ScopeType.SESSION)
@Table(name="USERS")
public class User implements Serializable {

    private Long id;
    private Integer version;
    private String name;
	private String password;
	private String realName;
	private Set<Contact> contacts;
	private Set<Registration> registrations;
	private Set<History> history;
	private Set<Role> roles;

    @Id @GeneratedValue
    @Column(name="USERID")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Version
    public Integer getVersion() {
        return version;
    }

    private void setVersion(Integer version) {
        this.version = version;
    }

    @Length(max = 20)
    @Column(unique=true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	@OneToMany(mappedBy="user",cascade = {CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH},fetch=FetchType.EAGER)
	public Set<Contact> getContacts() {
		return contacts;
	}

	public void setContacts(Set<Contact> contacts) {
		this.contacts = contacts;
	}

	@OneToMany(mappedBy="user",cascade={CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH},fetch=FetchType.EAGER)
	public Set<Registration> getRegistrations() {
		return registrations;
	}

	public void setRegistrations(Set<Registration> registrations) {
		this.registrations = registrations;
	}

	@OneToMany(mappedBy="user",cascade={CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH},fetch=FetchType.EAGER)
	public Set<History> getHistory() {
		return history;
	}

	public void setHistory(Set<History> history) {
		this.history = history;
	}
	
	@OneToMany(mappedBy="user",cascade={CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH},fetch=FetchType.EAGER)
	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
	
}
