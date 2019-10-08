package com.ricequant.rqboot.lang.lambda;

/**
 * @author chenfeng
 */
public interface LongBiConsumer<T> {

  void accept(long result, T err);
}
