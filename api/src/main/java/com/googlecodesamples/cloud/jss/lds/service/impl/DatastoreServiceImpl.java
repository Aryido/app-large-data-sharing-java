package com.googlecodesamples.cloud.jss.lds.service.impl;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.googlecodesamples.cloud.jss.lds.model.Dao;
import com.googlecodesamples.cloud.jss.lds.model.FileMeta;
import com.googlecodesamples.cloud.jss.lds.service.IConvertorService;
import com.googlecodesamples.cloud.jss.lds.service.IDatastoreService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DatastoreServiceImpl implements IDatastoreService<Dao> {
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
}
