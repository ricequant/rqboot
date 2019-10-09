package com.ricequant.rqboot.config.processes;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author chenfeng
 */
public class RQRelease {

  private String iName;

  private String iDir;

  public String getName() {
    return iName;
  }

  @JSONField(name = "Name")
  public void setName(String name) {
    iName = name;
  }

  public String getDir() {
    return iDir;
  }

  @JSONField(name = "Dir")
  public void setDir(String dir) {
    iDir = dir;
  }
}
