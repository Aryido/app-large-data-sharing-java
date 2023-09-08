package com.googlecodesamples.cloud.jss.lds.service.impl;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.googlecodesamples.cloud.jss.lds.model.Vo;
import com.googlecodesamples.cloud.jss.lds.service.IConvertorService;
import com.googlecodesamples.cloud.jss.lds.util.LdsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class ConvertorServiceImpl implements IConvertorService<QueryDocumentSnapshot, Vo> {
	private static final String THUMBNAIL_EXTENSION = "_small";
	@Value("${resource.path}")
	private String basePath;

	@Override
	public Vo convert(QueryDocumentSnapshot sourceData) {
		Vo vo = sourceData.toObject(Vo.class);

		String path = vo.getPath();
		String resourceBasePath = LdsUtil.getResourceBasePath(basePath);
		vo.setUrl(resourceBasePath + path);
		vo.setThumbUrl(resourceBasePath + path + THUMBNAIL_EXTENSION);
		vo.setCreateTime(Objects.requireNonNull(sourceData.getCreateTime()).toDate());
		vo.setUpdateTime(Objects.requireNonNull(sourceData.getUpdateTime()).toDate());

		return vo;
	}
}
