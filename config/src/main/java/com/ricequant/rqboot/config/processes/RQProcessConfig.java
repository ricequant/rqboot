package com.ricequant.rqboot.config.processes;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author chenfeng
 */
public class RQProcessConfig {

  private String iName;

  private String iBinary;

  private String iRelease;

  private String iArgs;

  private String iDebug;

  private String iJvmProperties;

  @JSONField(name = "Release")
  public void setRelease(String release) {
    iRelease = release;
  }

  @JSONField(name = "JvmProperties")
  public void setJvmProperties(String jvmProperties) {
    iJvmProperties = jvmProperties;
  }

  public String getName() {
    return iName;
  }

  @JSONField(name = "Name")
  public void setName(String name) {
    iName = name;
  }

  public String getBinary() {
    return iBinary;
  }

  @JSONField(name = "Binary")
  public void setBinary(String binary) {
    iBinary = binary;
  }

  public String getRelealse() {
    return iRelease;
  }

  public String getArgs() {
    return iArgs;
  }

  @JSONField(name = "Args")
  public void setArgs(String args) {
    iArgs = args;
  }

  public String getJVMProperties() {
    return iJvmProperties;
  }

  public String getDebug() {
    return iDebug;
  }

  @JSONField(name = "Debug")
  public void setDebug(String debug) {
    iDebug = debug;
  }
}
