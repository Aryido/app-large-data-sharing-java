package com.googlecodesamples.cloud.jss.lds.service;

import com.googlecodesamples.cloud.jss.lds.model.BaseFile;
import com.googlecodesamples.cloud.jss.lds.model.Vo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface IFileService<F> {

	List<F> createFiles(List<MultipartFile> files, List<String> tags);

	F updateFile(MultipartFile newFile, List<String> tags, F oldVo);

	void deleteFile(F file);

	List<F> findFilesBy(List<String> tags, String orderNo, int size);

	Optional<F> findFileById(String fileId);

	void resetFile();

}
