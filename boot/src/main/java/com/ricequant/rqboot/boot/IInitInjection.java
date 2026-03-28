package com.ricequant.rqboot.boot;

/**
 * @author newman
 */
public interface IInitInjection {

  void set(IApplication2 application);

  void init();
}
