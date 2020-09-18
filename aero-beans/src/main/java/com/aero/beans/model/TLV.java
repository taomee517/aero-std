package com.aero.beans.model;

import lombok.Data;

/**
 * @author 罗涛
 * @title TLV
 * @date 2020/9/14 14:51
 */
@Data
public class TLV {
    private int type;
    private int length;
    private byte[] value;

    public TLV() {
    }

    public TLV(int type, int length, byte[] value) {
        this.type = type;
        this.length = length;
        this.value = value;
    }
}
