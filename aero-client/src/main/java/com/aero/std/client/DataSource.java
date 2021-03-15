package com.aero.std.client;

import com.aero.beans.constants.EnvType;
import com.aero.std.handler.AeroDevice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Component
public class DataSource {
//    @Value("${stress.test.devices.path}")
//    String devicesPath;

    @Value("${protocol.env.type}")
    String env;

    @PostConstruct
    public void loadDevices() throws Exception {
//        List<String> snList = DeviceSourceUtil.getColumnData(devicesPath);

//        List<String> imeis = Arrays.asList("813463346541368");
//        List<String> imeis = Arrays.asList("813463346584699");
        List<String> imeis = Arrays.asList("813463346584610");
//        List<String> imeis = Arrays.asList("813463346541312");

        for (String imei : imeis) {
            AeroDevice device = new AeroDevice(imei, EnvType.valueOf(env));
        }
    }
}
