package com.googlecodesamples.cloud.jss.lds.service;

import java.util.List;
import java.util.Optional;

public interface IDatastoreService<D, V> {

	void save(D data);

	void delete(String dataId);

	void deleteAll();

	Optional<D> findById(String dataId);

	List<V> findAllOrderByOrderNoDescLimit(int limit);

	List<V> findByTagsContainOrderByOrderNoDescLimit(List<String> tags, int limit);

	List<V> findByTagsContainOrderByOrderNoDescStartAfterOrderNoLimit(List<String> tags, String orderNo, int limit);

}
