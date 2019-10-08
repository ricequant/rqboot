package com.ricequant.rqboot.lang;

/**
 * @author chenfeng
 */
public interface ITwoObjectsKey<ObjectType1, ObjectType2> {

  ObjectType1 getFirst();

  ObjectType2 getSecond();
}
