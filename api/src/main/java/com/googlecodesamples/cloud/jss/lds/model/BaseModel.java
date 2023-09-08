package com.googlecodesamples.cloud.jss.lds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseModel {
	private String id;
	private String path;
	private String name;
	private List<String> tags;
	private String orderNo;
	private long size;
}
