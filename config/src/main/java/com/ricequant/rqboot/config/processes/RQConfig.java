package com.ricequant.rqboot.config.processes;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author chenfeng
 */
public class RQConfig {

  private RQGlobal iGlobal;

  private List<RQProcessConfig> iProcesses;

  public static void main(String args[]) {
    try {
      fromConfigFile(new File("/tmp/rq.config"));
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static RQConfig fromConfigFile(File file) throws IOException {
    String rqConf = FileUtils.readFileToString(file);
    return JSON.parseObject(rqConf, RQConfig.class);
  }

  public RQGlobal getGlobal() {
    return iGlobal;
  }

  @JSONField(name = "Global")
  public void setGlobal(RQGlobal global) {
    iGlobal = global;
  }

  public List<RQProcessConfig> getProcesses() {
    return iProcesses;
  }

  @JSONField(name = "Processes")
  public void setProcesses(List<RQProcessConfig> processes) {
    iProcesses = processes;
  }
}
