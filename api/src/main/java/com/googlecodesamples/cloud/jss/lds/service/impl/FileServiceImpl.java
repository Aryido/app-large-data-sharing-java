package com.googlecodesamples.cloud.jss.lds.service.impl;

import com.googlecodesamples.cloud.jss.lds.model.BaseFile;
import com.googlecodesamples.cloud.jss.lds.model.Dao;
import com.googlecodesamples.cloud.jss.lds.model.FileMeta;
import com.googlecodesamples.cloud.jss.lds.model.Vo;
import com.googlecodesamples.cloud.jss.lds.service.*;
import com.googlecodesamples.cloud.jss.lds.util.LdsUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileServiceImpl implements IFileService<Vo> {
	private static final int THUMBNAIL_SIZE = 300;

	@Value("${resource.path}")
	private String basePath;
	private final IDatastoreService<Dao, Vo> firestoreService;
	private final IBucketService storageService;

	public FileServiceImpl(IDatastoreService<Dao, Vo> firestoreService, IBucketService storageService) {
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
			Dao dao = Dao.builder().id(fileId).path(fileBucketPath).name(file.getOriginalFilename()).tags(tags)
					.size(file.getSize()).build();
			firestoreService.save(dao);
			storageService.save(fileBucketPath, file.getContentType(), file.getBytes());

			fileIdList.add(fileId);
		}
		return fileIdList.stream().map(this::findFileById).filter(Optional::isPresent).map(Optional::get)
				.collect(Collectors.toList());
	}

	@Override
	public Vo updateFile(MultipartFile newFile, List<String> tags, Vo oldVo) {
		if (Objects.nonNull(newFile)) {
			Dao dao = Dao.builder().id(oldVo.getId()).path(oldVo.getPath()).name(oldVo.getName()).tags(tags)
					.size(oldVo.getSize()).build();
			firestoreService.save(dao);
			return null;
		} else {
//			storageService.delete(oldVo.getPath());
//			storageService.delete(oldVo.genThumbnailPath());
//			String newFileId = LdsUtil.generateUuid();
//			return createOrUpdateFile(newFile, tags, fileId, newFileId, newFile.getSize());
			return null;
		}
	}

	@Override
	public void deleteFile(Vo file) {

	}

	@Override
	public List<Vo> findFilesBy(List<String> tags, String orderNo, int size) {
		return null;
	}

	@Override
	public Optional<Vo> findFileById(String fileId) {
		log.info("Start to find file by id:{}", fileId);
		return firestoreService.findById(fileId);
	}

	@Override
	public void resetFile() {

	}
}
