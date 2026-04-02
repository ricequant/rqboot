package com.ricequant.rqboot.management.server;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public enum ManagementCommandArgType {

  LONG("long") {
    @Override
    protected Object convertValue(Object value, String argName) {
      return asLong(value, argName);
    }
  },
  DOUBLE("double") {
    @Override
    protected Object convertValue(Object value, String argName) {
      return asDouble(value, argName);
    }
  },
  STRING("string") {
    @Override
    protected Object convertValue(Object value, String argName) {
      return asString(value, argName);
    }
  },
  LONG_ARRAY("long[]") {
    @Override
    protected Object convertValue(Object value, String argName) {
      List<Object> items = asList(value, argName);
      long[] result = new long[items.size()];
      for (int i = 0; i < items.size(); i++) {
        result[i] = asLong(items.get(i), argName);
      }
      return result;
    }
  },
  DOUBLE_ARRAY("double[]") {
    @Override
    protected Object convertValue(Object value, String argName) {
      List<Object> items = asList(value, argName);
      double[] result = new double[items.size()];
      for (int i = 0; i < items.size(); i++) {
        result[i] = asDouble(items.get(i), argName);
      }
      return result;
    }
  },
  STRING_ARRAY("string[]") {
    @Override
    protected Object convertValue(Object value, String argName) {
      List<Object> items = asList(value, argName);
      String[] result = new String[items.size()];
      for (int i = 0; i < items.size(); i++) {
        result[i] = asString(items.get(i), argName);
      }
      return result;
    }
  };

  private final String iId;

  ManagementCommandArgType(String id) {
    iId = id;
  }

  public String id() {
    return iId;
  }

  public Object convert(Object value, String argName) {
    if (value == null) {
      return null;
    }
    return convertValue(value, argName);
  }

  protected abstract Object convertValue(Object value, String argName);

  public static ManagementCommandArgType fromId(String id) {
    String normalized = id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
    for (ManagementCommandArgType type : values()) {
      if (type.id().equals(normalized)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unsupported command argument type: " + id);
  }

  private static long asLong(Object value, String argName) {
    if (value instanceof Number number) {
      return number.longValue();
    }
    if (value instanceof String text) {
      try {
        return Long.parseLong(text.trim());
      }
      catch (NumberFormatException e) {
        throw invalid(argName, "long", value);
      }
    }
    throw invalid(argName, "long", value);
  }

  private static double asDouble(Object value, String argName) {
    if (value instanceof Number number) {
      return number.doubleValue();
    }
    if (value instanceof String text) {
      try {
        return Double.parseDouble(text.trim());
      }
      catch (NumberFormatException e) {
        throw invalid(argName, "double", value);
      }
    }
    throw invalid(argName, "double", value);
  }

  private static String asString(Object value, String argName) {
    if (value instanceof Iterable<?> || value.getClass().isArray()) {
      throw invalid(argName, "string", value);
    }
    return String.valueOf(value);
  }

  private static List<Object> asList(Object value, String argName) {
    if (value instanceof Iterable<?> iterable) {
      List<Object> items = new ArrayList<>();
      for (Object item : iterable) {
        items.add(item);
      }
      return items;
    }

    if (value.getClass().isArray()) {
      int length = Array.getLength(value);
      List<Object> items = new ArrayList<>(length);
      for (int i = 0; i < length; i++) {
        items.add(Array.get(value, i));
      }
      return items;
    }

    throw invalid(argName, "array", value);
  }

  private static IllegalArgumentException invalid(String argName, String type, Object value) {
    return new IllegalArgumentException(
            "Argument <" + argName + "> expects type " + type + ", actual value: " + String.valueOf(value));
  }
}
