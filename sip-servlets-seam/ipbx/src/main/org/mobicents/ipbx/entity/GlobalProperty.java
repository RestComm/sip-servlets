package org.mobicents.ipbx.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name="GLOBALPROPERTIES")
public class GlobalProperty implements Serializable {

    private Long id;
    private Integer version;
    private String name;
    private String value;


    @Id @GeneratedValue
    @Column(name="GLOBALPROPERTYID")
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
