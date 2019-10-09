package com.ricequant.rqboot.config.config_objects;

import com.ricemap.utilities.config.EndPointType;
import com.ricequant.rqboot.config.CommonProperties;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;

/**
 * @author chenfeng
 */
public class EndPointConfig {

  private ByteOrder iByteOrder = ByteOrder.LITTLE_ENDIAN;

  private int iInitialReadBufferSize = 5000;

  private int iSendBufferSize = 2 * 1024 * 1024;

  private int iMessageHeaderLength = 16;

  private int iBacklog = 10240;

  private InetSocketAddress iAddress;

  private int iHeartBeatIntervalMillis = CommonProperties.communicationHeartBeatInterval();

  private boolean iBlocking;


  private EndPointConfig() {

  }

  public EndPointConfig byteOrder(ByteOrder order) {
    iByteOrder = order;
    return this;
  }

  public ByteOrder byteOrder() {
    return iByteOrder;
  }

  public EndPointConfig initialReadBufferSize(int size) {
    iInitialReadBufferSize = size;
    return this;
  }

  public int initialReadBufferSize() {
    return iInitialReadBufferSize;
  }

  public EndPointConfig sendBufferSize(int sizeInBytes) {
    iSendBufferSize = sizeInBytes;
    return this;
  }

  public int sendBufferSize() {
    return iSendBufferSize;
  }

  public int backlog() {
    return iBacklog;
  }

  public EndPointConfig backlog(int backlog) {
    iBacklog = backlog;
    return this;
  }

  public EndPointConfig address(InetSocketAddress address) {
    iAddress = address;
    return this;
  }

  public InetSocketAddress address() {
    return iAddress;
  }

  public EndPointConfig blocking(boolean blocking) {
    iBlocking = blocking;
    return this;
  }

  public boolean blocking() {
    return iBlocking;
  }

  public int heartBeatIntervalMillis() {
    return iHeartBeatIntervalMillis;
  }

  public EndPointConfig heartBeatIntervalMillis(int millis) {
    System.out.println("Set heartbeat interval: " + millis);
    iHeartBeatIntervalMillis = millis;
    return this;
  }

  public EndPointConfig headerLength(int length) {
    iMessageHeaderLength = length;
    return this;
  }

  public int headerLength() {
    return iMessageHeaderLength;
  }

  public static EndPointConfig blocking(InetSocketAddress address) {
    return new EndPointConfig().blocking(true).address(address);
  }

  public static EndPointConfig fromConfig(EndPointType config) {
    return new EndPointConfig().blocking(true).address(new InetSocketAddress(config.getHost(), config.getPort()))
            .sendBufferSize((config.getSendBuffer())).heartBeatIntervalMillis(
                    config.getHeartBeatIntervalMillis() == null ? CommonProperties.communicationHeartBeatInterval()
                            : config.getHeartBeatIntervalMillis());
  }

  @Override
  public boolean equals(Object o) {
    if (super.equals(o))
      return true;

    if (o == null)
      return false;

    if (!(o instanceof EndPointConfig))
      return false;

    EndPointConfig that = (EndPointConfig) o;

    return iByteOrder == that.iByteOrder && ObjectUtils.equals(iAddress, that.iAddress) && iBacklog == that.iBacklog
            && iBlocking == that.iBlocking && iMessageHeaderLength == that.iMessageHeaderLength &&
            iSendBufferSize == that.iSendBufferSize && iInitialReadBufferSize == that.iInitialReadBufferSize;
  }

  @Override
  public int hashCode() {
    HashCodeBuilder b = new HashCodeBuilder();
    return b.append(iBacklog).append(iAddress).append(iBlocking).append(iByteOrder).append(iMessageHeaderLength)
            .append(iSendBufferSize).append(iInitialReadBufferSize).append(iHeartBeatIntervalMillis).build();
  }

  @Override
  public String toString() {
    return "{byteOrder:\"" + iByteOrder + "\",blocking:" + iBlocking + ",backlog:" + iBacklog + "," + "headerLength:"
            + iMessageHeaderLength + ",sendBufferSize:\"" + iSendBufferSize + "\"initialReadBufferSize:"
            + iInitialReadBufferSize + ",address=\"" + iAddress + "\",heartBeatIntervalMillis:"
            + iHeartBeatIntervalMillis + "}";
  }
}
