package com.ricequant.rqboot.jmx.shared_resource.registry;

import java.util.Arrays;
import java.util.function.Function;

/**
 * @author chenfeng
 */
public enum JmxTypeEnum {
  BOOLEAN(boolean.class.getName(), Boolean::parseBoolean),

  BYTE(byte.class.getName(), Byte::parseByte),

  SHORT(short.class.getName(), Short::parseShort),

  INTEGER(int.class.getName(), Integer::parseInt),

  LONG(long.class.getName(), Long::parseLong),

  FLOAT(float.class.getName(), Float::parseFloat),

  DOUBLE(double.class.getName(), Double::parseDouble),

  STRING(String.class.getName(), in -> in),

  OBJECT(Object.class.getName(), in -> in),

  VOID(void.class.getName(), in -> in),

  BOOLEAN_ARRAY(boolean[].class.getName(), in -> Arrays.asList(in.substring(1, in.length() - 1).split(",")).stream()
          .map(ele -> Boolean.parseBoolean(ele.trim())).toArray()),

  BYTE_ARRAY(byte[].class.getName(), in -> Arrays.asList(in.substring(1, in.length() - 1).split(",")).stream()
          .map(ele -> Byte.parseByte(ele.trim())).toArray()),

  SHORT_ARRAY(short[].class.getName(), in -> Arrays.asList(in.substring(1, in.length() - 1).split(",")).stream()
          .map(ele -> Short.parseShort(ele.trim())).toArray()),

  INTEGER_ARRAY(int[].class.getName(), in -> Arrays.asList(in.substring(1, in.length() - 1).split(",")).stream()
          .map(ele -> Integer.parseInt(ele.trim())).toArray()),

  LONG_ARRAY(long[].class.getName(), in -> Arrays.asList(in.substring(1, in.length() - 1).split(",")).stream()
          .map(ele -> Long.parseLong(ele.trim())).toArray()),

  FLOAT_ARRAY(float[].class.getName(), in -> Arrays.asList(in.substring(1, in.length() - 1).split(",")).stream()
          .map(ele -> Float.parseFloat(ele.trim())).toArray()),

  DOUBLE_ARRAY(double[].class.getName(), in -> Arrays.asList(in.substring(1, in.length() - 1).split(",")).stream()
          .map(ele -> Double.parseDouble(ele.trim())).toArray()),

  STRING_ARRAY(String[].class.getName(),
          in -> Arrays.asList(in.substring(1, in.length() - 1).split(",")).stream().map(String::trim).toArray());

  private final String iSignature;

  private final Function<String, Object> iConverter;

  JmxTypeEnum(String signature, Function<String, Object> converter) {
    iSignature = signature;
    iConverter = converter;
  }

  public static JmxTypeEnum fromType(Class<?> type) {
    if (Boolean.class.equals(type) || boolean.class.equals(type))
      return BOOLEAN;

    else if (Byte.class.equals(type) || byte.class.equals(type))
      return BYTE;

    else if (Short.class.equals(type) || short.class.equals(type))
      return SHORT;

    else if (Integer.class.equals(type) || int.class.equals(type))
      return INTEGER;

    else if (Long.class.equals(type) || long.class.equals(type))
      return LONG;

    else if (Float.class.equals(type) || float.class.equals(type))
      return FLOAT;

    else if (Double.class.equals(type) || double.class.equals(type))
      return DOUBLE;

    else if (String.class.equals(type))
      return STRING;

    else if (Void.class.equals(type) || void.class.equals(type))
      return VOID;

    if (Boolean[].class.equals(type) || boolean[].class.equals(type))
      return BOOLEAN_ARRAY;

    else if (Byte[].class.equals(type) || byte[].class.equals(type))
      return BYTE_ARRAY;

    else if (Short[].class.equals(type) || short[].class.equals(type))
      return SHORT_ARRAY;

    else if (Integer[].class.equals(type) || int[].class.equals(type))
      return INTEGER_ARRAY;

    else if (Long[].class.equals(type) || long[].class.equals(type))
      return LONG_ARRAY;

    else if (Float[].class.equals(type) || float[].class.equals(type))
      return FLOAT_ARRAY;

    else if (Double[].class.equals(type) || double[].class.equals(type))
      return DOUBLE_ARRAY;

    else if (String[].class.equals(type))
      return STRING_ARRAY;
    else
      return OBJECT;
  }

  public String getSignature() {
    return iSignature;
  }

  public Object fromString(String var) {
    return iConverter.apply(var);
  }
}
