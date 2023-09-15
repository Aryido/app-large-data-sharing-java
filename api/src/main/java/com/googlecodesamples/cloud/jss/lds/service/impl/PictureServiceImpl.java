package com.googlecodesamples.cloud.jss.lds.service.impl;

import com.googlecodesamples.cloud.jss.lds.repository.IBucketRepository;
import com.googlecodesamples.cloud.jss.lds.service.IPictureService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class PictureServiceImpl implements IPictureService {

	private static final List<String> IMG_EXTENSIONS = List.of("png", "jpeg", "jpg", "gif");
	private static final String THUMBNAIL_EXTENSION = "_small";
	private static final int THUMBNAIL_SIZE = 300;

	private final IBucketRepository storageService;

	public PictureServiceImpl(IBucketRepository storageService) {
		this.storageService = storageService;
	}

	@SneakyThrows
	@Override
	public void savePicture(String Filename, String pathWithDataId, String contentType, byte[] content) {

		storageService.save(pathWithDataId, contentType, content);
		if (checkImageFileType(Filename)) {
			storageService.save(genThumbnailPath(pathWithDataId), contentType, createThumbnail(content));
		}
	}

	@SneakyThrows
	@Override
	public void updatePicture(String Filename, String oldPathWithDataId, String newPathWithDataId, String contentType,
			byte[] content) {
		deletePicture(oldPathWithDataId);
		savePicture(Filename, newPathWithDataId, contentType, content);
	}

	@Override
	public void deletePicture(String pathWithDataId) {
		storageService.delete(pathWithDataId);
		storageService.delete(genThumbnailPath(pathWithDataId));
	}

	@Override
	public void deleteAllPictures() {
		storageService.deleteAll();
	}

	private boolean checkImageFileType(String ImageFileName) {
		return IMG_EXTENSIONS.stream().anyMatch(e -> ImageFileName.toLowerCase().endsWith(e));
	}

	private String genThumbnailPath(String path) {
		return path + THUMBNAIL_EXTENSION;
	}

	@SneakyThrows
	private byte[] createThumbnail(byte[] content) {
		try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content);
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();) {
			Thumbnails.of(byteArrayInputStream).size(THUMBNAIL_SIZE, THUMBNAIL_SIZE).keepAspectRatio(false)
					.toOutputStream(byteArrayOutputStream);
			return byteArrayOutputStream.toByteArray();
		} catch (IOException ioException) {
			log.error("Create thumbnail failed");
			throw new RuntimeException(ioException.getMessage());
		}
	}
}
