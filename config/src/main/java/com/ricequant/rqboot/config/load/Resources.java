package com.ricequant.rqboot.config.load;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author kain
 */
public class Resources {

  public static Properties loadProperties(String filePath) throws IOException {
    Properties prop = new Properties();
    FileInputStream fis = new FileInputStream(new File(filePath));
    prop.load(fis);
    fis.close();
    return prop;
  }
}
