package com.googlecodesamples.cloud.jss.lds.service;

public interface IConvertorService<F, V> {
	V convert(F sourceData);
}
