package com.googlecodesamples.cloud.jss.lds.service;
import java.util.Optional;

public interface IDatastoreService<T> {

	void save(T data);

	void delete(String dataId);

	void deleteAll();

	Optional<T> findById(String dataId);

}
