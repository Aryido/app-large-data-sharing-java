package com.googlecodesamples.cloud.jss.lds.service;

public interface IPictureService {

	void savePicture(String pathWithDataId, String contentType, byte[] content);

	void updatePicture(String oldPathWithDataId, String newPathWithDataId, String contentType, byte[] content);

	void deletePicture(String pathWithDataId);

	void deleteAllPictures();
}
