package com.googlecodesamples.cloud.jss.lds.repository;

public interface IBucketRepository {

	void save(String pathWithDataId, String contentType, byte[] content);

	void delete(String pathWithDataId);

	void deleteAll();
}
