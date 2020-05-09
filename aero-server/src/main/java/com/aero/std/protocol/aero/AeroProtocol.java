package com.aero.std.protocol.aero;

import com.aero.std.protocol.IProtocol;
import org.springframework.stereotype.Component;

/**
 * @author 罗涛
 * @title AeroProtocol
 * @date 2020/5/8 14:34
 */
@Component
public class AeroProtocol implements IProtocol {
    @Override
    public int port() {
        return 34251;
    }
}
