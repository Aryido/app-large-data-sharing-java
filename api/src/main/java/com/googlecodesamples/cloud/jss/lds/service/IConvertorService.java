package com.googlecodesamples.cloud.jss.lds.service;

import java.util.Map;

public interface IConvertorService<F, V> {
	V convert(F sourceData);
}
