package com.krishagni.catissueplus.core.common.repository;

public interface UniqueIdGenerator {
	Long getUniqueId(String type, String id);
}
