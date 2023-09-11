package com.googlecodesamples.cloud.jss.lds.service.impl;

import com.googlecodesamples.cloud.jss.lds.repository.IBucketRepository;
import com.googlecodesamples.cloud.jss.lds.service.IPictureService;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

	@Override
	public void savePicture(String pathWithDataId, String contentType, byte[] content) {
		storageService.save(pathWithDataId, contentType, content);
//		if (checkImageFileType(file.getOriginalFilename())) {
//			storageService.save(genThumbnailPath(fileBucketPath), file.getContentType(), createThumbnail(file));
//		}
	}

	@Override
	public void updatePicture(String oldPathWithDataId, String newPathWithDataId, String contentType, byte[] content) {
		storageService.delete(oldPathWithDataId);
		storageService.delete(genThumbnailPath(oldPathWithDataId));
		storageService.save(newPathWithDataId, contentType, content);
		//storageService.save(genThumbnailPath(newPathWithDataId), contentType, createThumbnail(content));
	}

	@Override
	public void deletePicture(String pathWithDataId) {
		storageService.delete(pathWithDataId);
		storageService.delete(genThumbnailPath(pathWithDataId));

	}

	@Override
	public void deleteAllPictures() {

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
