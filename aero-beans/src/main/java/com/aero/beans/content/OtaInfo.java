package com.aero.beans.content;

import lombok.Data;

/**
 * @author 罗涛
 * @title OtaInfo
 * @date 2021/3/18 17:21
 */
@Data
public class OtaInfo {
    String hardware;
    String hardwareVersion;
    String software;
    String softwareVersion;
    String md5;
    String url;
    Integer fileLength;
}
