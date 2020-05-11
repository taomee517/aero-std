package com.aero.std.common.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class SnUtil {
    private  static AtomicInteger atomitSn = new AtomicInteger(1);

    public static int getSn(){
        int sn = atomitSn.getAndAdd(1);
        return sn & 0xffff;
    }
}
