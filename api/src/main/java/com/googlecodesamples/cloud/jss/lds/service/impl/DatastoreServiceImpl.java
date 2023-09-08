package com.googlecodesamples.cloud.jss.lds.service.impl;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.googlecodesamples.cloud.jss.lds.model.Entity;
import com.googlecodesamples.cloud.jss.lds.model.Vo;
import com.googlecodesamples.cloud.jss.lds.service.IConvertorService;
import com.googlecodesamples.cloud.jss.lds.service.IDatastoreService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DatastoreServiceImpl implements IDatastoreService<Entity, Vo> {
	private static final String TAGS = "tags";
	private static final String ORDER_NO = "orderNo";
	private final Firestore firestore;
	private final IConvertorService<QueryDocumentSnapshot, Vo> convertorService;
	@Value("${firestore.collection.name}")
	private String collectionName;

	public DatastoreServiceImpl(
			Firestore firestore,
			IConvertorService<QueryDocumentSnapshot, Vo> convertorService
	) {
		this.firestore = firestore;
		this.convertorService = convertorService;
	}

	@SneakyThrows
	@Override
	public void save(Entity data) {
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
	public Optional<Vo> findById(String dataId) {
		ApiFuture<QuerySnapshot> future = firestore.collection(collectionName)
				.whereEqualTo(FieldPath.documentId(), dataId)
				.get();
		List<QueryDocumentSnapshot> documents = future.get().getDocuments();
		return documents.stream()
				.map(convertorService::convert)
				.findFirst();
	}

	@SneakyThrows
	@Override
	public List<Vo> findAllOrderByOrderNoDescLimit(int limit) {
		ApiFuture<QuerySnapshot> future = firestore.collection(collectionName)
				.orderBy(ORDER_NO, Query.Direction.DESCENDING)
				.limit(limit)
				.get();
		List<QueryDocumentSnapshot> documents = future.get().getDocuments();
		return documents.stream()
				.map(convertorService::convert)
				.collect(Collectors.toList());
	}

	@SneakyThrows
	@Override
	public List<Vo> findByTagsContainOrderByOrderNoDescLimit(List<String> tags, int limit) {
		ApiFuture<QuerySnapshot> future = firestore.collection(collectionName)
				.whereArrayContainsAny(TAGS, tags)
				.orderBy(ORDER_NO, Query.Direction.DESCENDING)
				.limit(limit)
				.get();
		List<QueryDocumentSnapshot> documents = future.get().getDocuments();
		return documents.stream()
				.map(convertorService::convert)
				.collect(Collectors.toList());
	}

	@SneakyThrows
	@Override
	public List<Vo> findAllOrderByOrderNoDescStartAfterOrderNoLimit(String orderNo, int limit) {
		ApiFuture<QuerySnapshot> future = firestore.collection(collectionName)
				.orderBy(ORDER_NO, Query.Direction.DESCENDING)
				.startAfter(orderNo)
				.limit(limit)
				.get();
		List<QueryDocumentSnapshot> documents = future.get().getDocuments();
		return documents.stream()
				.map(convertorService::convert)
				.collect(Collectors.toList());
	}

	@SneakyThrows
	@Override
	public List<Vo> findByTagsContainOrderByOrderNoDescStartAfterOrderNoLimit(List<String> tags, String orderNo,
			int limit) {
		ApiFuture<QuerySnapshot> future = firestore.collection(collectionName)
				.whereArrayContainsAny(TAGS, tags)
				.orderBy(ORDER_NO, Query.Direction.DESCENDING)
				.startAfter(orderNo)
				.limit(limit)
				.get();
		List<QueryDocumentSnapshot> documents = future.get().getDocuments();
		return documents.stream()
				.map(convertorService::convert)
				.collect(Collectors.toList());
	}
}
