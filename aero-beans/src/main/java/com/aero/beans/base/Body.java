package com.aero.beans.base;

import com.aero.beans.content.DetectData;
import com.aero.beans.content.DeviceInfo;
import com.aero.beans.content.OtaInfo;
import lombok.Data;

/**
 * @author 罗涛
 * @title Body
 * @date 2020/5/12 17:19
 */
@Data
public class Body {
    private long deviceUtc;
    private long serverUtc;
    private String loginPwd;
    private long rebootCount;
    private Object coreData;
    private DetectData detectData;
    private DeviceInfo deviceInfo;
    private OtaInfo otaInfo;
}
