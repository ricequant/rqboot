package com.ricequant.rqboot.lang.lambda;

/**
 * @author chenfeng
 */
public interface IntBiConsumer<T> {

  void accept(int result, T err);
}
