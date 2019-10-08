package com.ricequant.rqboot.lang;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author chenfeng
 */
public class HashMapHelper {

  public static <ObjectType1, ObjectType2> ITwoObjectsKey<ObjectType1, ObjectType2> twoObjectsKey(
          ObjectType1 component1, ObjectType2 component2) {
    return new TwoObjectKey<>(component1, component2);
  }

  private static class TwoObjectKey<ObjectType1, ObjectType2> implements ITwoObjectsKey<ObjectType1, ObjectType2> {

    private final ObjectType1 iO1;

    private final ObjectType2 iO2;

    private final int iCode;

    TwoObjectKey(ObjectType1 o1, ObjectType2 o2) {
      iO1 = o1;
      iO2 = o2;

      HashCodeBuilder hashBuilder = new HashCodeBuilder();
      iCode = hashBuilder.append(o1).append(o2).toHashCode();
    }

    @Override
    public ObjectType1 getFirst() {
      return iO1;
    }

    @Override
    public ObjectType2 getSecond() {
      return iO2;
    }

    @Override
    public boolean equals(Object o) {
      if (o == null)
        return false;

      if (o.getClass() != getClass())
        return false;

      TwoObjectKey that = (TwoObjectKey) o;
      if (ObjectUtils.equals(iO1, that.iO1) && ObjectUtils.equals(iO2, that.iO2))
        return true;

      return false;
    }

    @Override
    public int hashCode() {
      return iCode;
    }

    @Override
    public String toString() {
      return "First: < " + iO1 + " >, Second: < " + iO2 + " >";
    }
  }
}
