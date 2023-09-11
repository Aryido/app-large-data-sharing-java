package com.googlecodesamples.cloud.jss.lds.repository.impl;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import com.googlecodesamples.cloud.jss.lds.repository.IBucketRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BucketRepositoryImpl implements IBucketRepository {
	@Value("${storage.bucket.name}")
	private String bucketName;
	private final Storage storage;
	public BucketRepositoryImpl(Storage storage) {
		this.storage = storage;
	}

	@Override
	public void save(String pathWithDataId, String contentType, byte[] content) {
		BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, pathWithDataId)
				.setContentType(contentType)
				.build();
		storage.create(blobInfo, content);
		log.info("Saved {} data in {} bucket.", pathWithDataId, bucketName);
	}

	@Override
	public void delete(String pathWithDataId) {
		storage.delete(bucketName, pathWithDataId);
		log.info("Deleted {} data in {} bucket.", pathWithDataId, bucketName);
	}

	@Override
	public void deleteAll() {
		Page<Blob> blobs = storage.list(bucketName);
		if (!blobs.getValues().iterator().hasNext()) {
			log.warn("No data in {} bucket.", bucketName);
			// if there are no blobs in the bucket, batch will be empty.
			// Then batchRequest.submit() will fail
			return;
		}
		StorageBatch batchRequest = storage.batch();
		for (Blob blob : blobs.iterateAll()) {
			batchRequest.delete(blob.getBlobId());
		}
		batchRequest.submit();
		log.warn("Delete all data in {} bucket.", bucketName);
	}
}
