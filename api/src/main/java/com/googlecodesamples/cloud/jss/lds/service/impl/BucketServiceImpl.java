package com.googlecodesamples.cloud.jss.lds.service.impl;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import com.googlecodesamples.cloud.jss.lds.service.IBucketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BucketServiceImpl implements IBucketService {
	@Value("${storage.bucket.name}")
	private String bucketName;
	private final Storage storage;
	public BucketServiceImpl(Storage storage) {
		this.storage = storage;
	}

	@Override
	public void save(String dataId, String contentType, byte[] content) {
		BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, dataId)
				.setContentType(contentType)
				.build();
		storage.create(blobInfo, content);
		log.info("Saved {} data in {} bucket.", dataId, bucketName);
	}

	@Override
	public void delete(String dataId) {
		storage.delete(bucketName, dataId);
		log.info("Deleted {} data in {} bucket.", dataId, bucketName);
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
