package com.googlecodesamples.cloud.jss.lds.service.impl;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.googlecodesamples.cloud.jss.lds.model.Dao;
import com.googlecodesamples.cloud.jss.lds.model.FileMeta;
import com.googlecodesamples.cloud.jss.lds.service.IConvertorService;
import com.googlecodesamples.cloud.jss.lds.service.IDatastoreService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DatastoreServiceImpl implements IDatastoreService<Dao, QueryDocumentSnapshot> {
	private static final String TAGS = "tags";

	private static final String ORDER_NO = "orderNo";
	private final Firestore firestore;
	@Value("${firestore.collection.name}")
	private String collectionName;

	@Value("${resource.path}")
	private String basePath;

	public DatastoreServiceImpl(Firestore firestore) {
		this.firestore = firestore;
	}

	@SneakyThrows
	@Override
	public void save(Dao data) {
		DocumentReference docRef = firestore.collection(collectionName).document(data.getId());
		docRef.set(data).get();
	}

	@SneakyThrows
	@Override
	public void delete(String dataId) {
		firestore.collection(collectionName).document(dataId).delete().get();
	}

	@SneakyThrows
	@Override
	public void deleteAll() {
		firestore.recursiveDelete(firestore.collection(collectionName)).get();
	}

	@SneakyThrows
	@Override
	public Optional<Dao> findById(String dataId) {
		ApiFuture<QuerySnapshot> future = firestore.collection(collectionName)
				.whereEqualTo(FieldPath.documentId(), dataId).get();
		List<QueryDocumentSnapshot> documents = future.get().getDocuments();
		return documents.stream().map(doc -> doc.toObject(Dao.class)).findFirst();
	}

	@Override
	public List<QueryDocumentSnapshot> findAllOrderByOrderNoDescLimit(int limit) {
		ApiFuture<QuerySnapshot> apiFuture = firestore.collection(collectionName)
				.orderBy(ORDER_NO, Query.Direction.DESCENDING)
				.limit(limit)
				.get();
		try {
			QuerySnapshot queryDocumentSnapshots = apiFuture.get();
			return queryDocumentSnapshots.getDocuments();
		} catch (InterruptedException | ExecutionException e) {
			log.error("find all data order by orderNo Desc Limit {} failed", limit);
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<QueryDocumentSnapshot> findByTagsContainOrderByOrderNoDescLimit(List<String> tags, int limit) {
		return null;
	}

	@Override
	public List<QueryDocumentSnapshot> findByTagsContainOrderByOrderNoDescStartAfterOrderNoLimit(List<String> tags,
			String orderNo, int limit) {
		return null;
	}
}
