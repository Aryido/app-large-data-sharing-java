package com.googlecodesamples.cloud.jss.lds.service;

public interface IPictureService {

	void savePicture(String Filename, String pathWithDataId, String contentType, byte[] content);

	void updatePicture(String Filename, String oldPathWithDataId, String newPathWithDataId, String contentType,  byte[] content);

	void deletePicture(String pathWithDataId);

	void deleteAllPictures();
}
