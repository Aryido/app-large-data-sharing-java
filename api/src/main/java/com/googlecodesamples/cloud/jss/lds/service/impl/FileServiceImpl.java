package com.googlecodesamples.cloud.jss.lds.service.impl;

import com.googlecodesamples.cloud.jss.lds.model.Entity;
import com.googlecodesamples.cloud.jss.lds.model.Vo;
import com.googlecodesamples.cloud.jss.lds.service.IDatastoreService;
import com.googlecodesamples.cloud.jss.lds.service.IFileService;
import com.googlecodesamples.cloud.jss.lds.service.IPictureService;
import com.googlecodesamples.cloud.jss.lds.util.LdsUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileServiceImpl implements IFileService<Vo> {
	@Value("${resource.path}")
	private String basePath;
	private final IDatastoreService<Entity, Vo> firestoreService;
	private final IPictureService pictureService;

	public FileServiceImpl(IDatastoreService<Entity, Vo> firestoreService, IPictureService pictureService) {
		this.firestoreService = firestoreService;
		this.pictureService = pictureService;
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
					.orderNo(String.format("%s-%s", System.currentTimeMillis(), fileId)).size(file.getSize()).build();
			firestoreService.save(entity);

			pictureService.savePicture(file.getOriginalFilename(), fileBucketPath, file.getContentType(),
					FileCopyUtils.copyToByteArray(file.getInputStream()));
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
					.orderNo(String.format("%s-%s", System.currentTimeMillis(), oldVo.getId())).size(oldVo.getSize())
					.build();
			firestoreService.save(entity);
		} else {

			String newFileBucketPath = LdsUtil.getFileBucketPath(basePath, LdsUtil.generateUuid());
			Entity entity = Entity.builder().id(oldVo.getId()).path(newFileBucketPath)
					.name(newFile.getOriginalFilename())
					.orderNo(String.format("%s-%s", System.currentTimeMillis(), oldVo.getId())).tags(tags)
					.size(newFile.getSize()).build();
			firestoreService.save(entity);
			pictureService.updatePicture(newFile.getOriginalFilename(), oldVo.getPath(), newFileBucketPath,
					newFile.getContentType(), FileCopyUtils.copyToByteArray(newFile.getInputStream()));
		}

		return findFileById(oldVo.getId()).orElseThrow(() -> new RuntimeException("File not found"));
	}

	@Override
	public void deleteFile(Vo file) {
		log.info("Start to create file id {}.", file.getId());
		firestoreService.delete(file.getId());
		pictureService.deletePicture(file.getPath());
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
		pictureService.deleteAllPictures();
	}

}
