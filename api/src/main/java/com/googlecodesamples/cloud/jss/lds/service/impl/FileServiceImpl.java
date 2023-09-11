package com.googlecodesamples.cloud.jss.lds.service.impl;

import com.googlecodesamples.cloud.jss.lds.model.Entity;
import com.googlecodesamples.cloud.jss.lds.model.Vo;
import com.googlecodesamples.cloud.jss.lds.repository.IBucketRepository;
import com.googlecodesamples.cloud.jss.lds.service.IDatastoreService;
import com.googlecodesamples.cloud.jss.lds.service.IFileService;
import com.googlecodesamples.cloud.jss.lds.util.LdsUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileServiceImpl implements IFileService<Vo> {
	private static final List<String> IMG_EXTENSIONS = List.of("png", "jpeg", "jpg", "gif");
	private static final String THUMBNAIL_EXTENSION = "_small";
	private static final int THUMBNAIL_SIZE = 300;

	@Value("${resource.path}")
	private String basePath;
	private final IDatastoreService<Entity, Vo> firestoreService;
	private final IBucketRepository storageService;

	public FileServiceImpl(IDatastoreService<Entity, Vo> firestoreService, IBucketRepository storageService) {
		this.firestoreService = firestoreService;
		this.storageService = storageService;
	}

	@SneakyThrows
	@Override
	public List<Vo> createFiles(List<MultipartFile> files, List<String> tags) {
		log.info("Start to create files");
		List<String> fileIdList = new ArrayList<>();
		for (MultipartFile file : files) {
			String fileId = LdsUtil.generateUuid();
			String fileBucketPath = LdsUtil.getFileBucketPath(basePath, fileId);
			Entity entity = Entity.builder().id(fileId).path(fileBucketPath).name(file.getOriginalFilename()).tags(tags)
					.orderNo(String.format("%s-%s", System.currentTimeMillis(), fileId))
					.size(file.getSize()).build();
			firestoreService.save(entity);


			//storageService.save(fileBucketPath, file.getContentType(), file.getBytes());
			storageService.save(fileBucketPath, file.getContentType(),FileCopyUtils.copyToByteArray(file.getInputStream()));
			if (checkImageFileType(file.getOriginalFilename())) {
				storageService.save(genThumbnailPath(fileBucketPath), file.getContentType(), createThumbnail(file));
			}
			fileIdList.add(fileId);
		}
		return fileIdList.stream().map(this::findFileById).filter(Optional::isPresent).map(Optional::get)
				.collect(Collectors.toList());
	}

	@SneakyThrows
	@Override
	public Vo updateFile(MultipartFile newFile, List<String> tags, Vo oldVo) {
		if (Objects.isNull(newFile)) {
			Entity entity = Entity.builder().id(oldVo.getId()).path(oldVo.getPath()).name(oldVo.getName()).tags(tags)
					.orderNo(String.format("%s-%s", System.currentTimeMillis(), oldVo.getId()))
					.size(oldVo.getSize()).build();
			firestoreService.save(entity);
		} else {
			storageService.delete(oldVo.getPath());
			storageService.delete(genThumbnailPath(oldVo.getPath()));

			String newFileBucketPath = LdsUtil.getFileBucketPath(basePath, LdsUtil.generateUuid());
			Entity entity = Entity.builder().id(oldVo.getId()).path(newFileBucketPath).name(newFile.getOriginalFilename())
					.orderNo(String.format("%s-%s", System.currentTimeMillis(), oldVo.getId()))
					.tags(tags).size(newFile.getSize()).build();
			firestoreService.save(entity);
			storageService.save(newFileBucketPath, newFile.getContentType(), newFile.getBytes());
			if (checkImageFileType(newFile.getOriginalFilename())) {
				storageService.save(genThumbnailPath(newFileBucketPath), newFile.getContentType(),
						createThumbnail(newFile));
			}
		}

		return findFileById(oldVo.getId()).orElseThrow(() -> new RuntimeException("File not found"));
	}

	@Override
	public void deleteFile(Vo file) {
		log.info("Start to create file id {}.", file.getId());
		firestoreService.delete(file.getId());
		storageService.delete(file.getPath());
		storageService.delete(genThumbnailPath(file.getPath()));
	}

	@Override
	public List<Vo> findFilesBy(List<String> tags, String orderNo, int size) {
		boolean hasTags = !CollectionUtils.isEmpty(tags);
		boolean hasOrderNo = StringUtils.hasText(orderNo);

		if (hasTags && hasOrderNo) {
			return firestoreService.findByTagsContainOrderByOrderNoDescStartAfterOrderNoLimit(tags, orderNo, size);
		}

		if (hasTags) {
			return firestoreService.findByTagsContainOrderByOrderNoDescLimit(tags, size);
		}

		if (hasOrderNo) {
			return firestoreService.findAllOrderByOrderNoDescStartAfterOrderNoLimit(orderNo, size);
		}

		return firestoreService.findAllOrderByOrderNoDescLimit(size);
	}

	@Override
	public Optional<Vo> findFileById(String fileId) {
		log.info("Start to find file by id:{}", fileId);
		return firestoreService.findById(fileId);
	}

	@Override
	public void resetFile() {
		log.warn("Start to reset data from datastore and bucket");
		firestoreService.deleteAll();
		storageService.deleteAll();
	}

	private boolean checkImageFileType(String ImageFileName) {
		return IMG_EXTENSIONS.stream().anyMatch(e -> ImageFileName.toLowerCase().endsWith(e));
	}

	private String genThumbnailPath(String path) {
		return path + THUMBNAIL_EXTENSION;
	}

	private byte[] createThumbnail(MultipartFile file) {
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				InputStream fileInputStream = file.getInputStream();) {
			Thumbnails.of(fileInputStream).size(THUMBNAIL_SIZE, THUMBNAIL_SIZE).keepAspectRatio(false)
					.toOutputStream(byteArrayOutputStream);
			return byteArrayOutputStream.toByteArray();
		} catch (IOException ioException) {
			log.error("Create thumbnail failed from {}", file.getOriginalFilename());
			throw new RuntimeException(ioException.getMessage());
		}
	}
}
