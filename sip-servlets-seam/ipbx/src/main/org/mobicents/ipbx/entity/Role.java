package org.mobicents.ipbx.entity;
import java.io.Serializable;

import javax.persistence.*;

import org.jboss.seam.annotations.Name;

@Entity
@Table(name="ROLES")
public class Role implements Serializable {
	private String role;
	private int id;
	private User user;
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
	@Column(name="ROLEID")
	public int getId() {
	     return id;
	}
	public void setId(int id) {
	     this.id = id;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
	
}
