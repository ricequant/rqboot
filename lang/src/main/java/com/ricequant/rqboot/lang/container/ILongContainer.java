package com.ricequant.rqboot.lang.container;

/**
 * @author chenfeng
 */
public interface ILongContainer {

  class NaiveContainer implements ILongContainer {

    private long iValue;

    public NaiveContainer() {

    }

    public NaiveContainer(long defaultValue) {
      iValue = defaultValue;
    }

    @Override
    public void set(long value) {
      iValue = value;
    }

    @Override
    public long get() {
      return iValue;
    }
  }

  void set(long value);

  long get();
}
