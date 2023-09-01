package com.googlecodesamples.cloud.jss.lds.service;

public interface IBucketService {

	void save(String dataId, String contentType, byte[] content);

	void delete(String dataId);

	void deleteAll();
}
