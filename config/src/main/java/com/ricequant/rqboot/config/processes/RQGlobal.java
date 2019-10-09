package com.ricequant.rqboot.config.processes;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * @author chenfeng
 */
public class RQGlobal {

  private String iTemp;

  private String iLogDir;

  private List<RQRelease> iRQRelease;

  @JSONField(name = "RELEASE")
  public void setRelease(List<RQRelease> releases) {
    iRQRelease = releases;
  }

  public String getTemp() {
    return iTemp;
  }

  @JSONField(name = "TEMP")
  public void setTemp(String temp) {
    iTemp = temp;
  }

  public String getLogDir() {
    return iLogDir;
  }

  @JSONField(name = "LOGDIR")
  public void setLogDir(String logDir) {
    iLogDir = logDir;
  }

  public List<RQRelease> getReleases() {
    return iRQRelease;
  }
}
