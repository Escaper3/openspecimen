
package com.krishagni.catissueplus.core.administrative.domain;

import java.util.HashSet;
import java.util.Set;

import com.krishagni.catissueplus.core.biospecimen.domain.BaseEntity;

public class Department extends BaseEntity {
	private String name;

	private Institute institute;

	private Set<User> userCollection = new HashSet<User>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Institute getInstitute() {
		return institute;
	}

	public void setInstitute(Institute institute) {
		this.institute = institute;
	}

	public Set<User> getUserCollection() {
		return userCollection;
	}

	public void setUserCollection(Set<User> userCollection) {
		this.userCollection = userCollection;
	}

}
