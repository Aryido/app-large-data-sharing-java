package com.googlecodesamples.cloud.jss.lds.service;

public interface IConvertorService<F, T> {

	T convert(F sourceData);

}
