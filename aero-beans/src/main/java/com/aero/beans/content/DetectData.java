package com.aero.beans.content;

import lombok.Data;

import java.util.List;

/**
 * @author 罗涛
 * @title DetectData
 * @date 2020/6/15 15:32
 */
@Data
public class DetectData {
    private List<Float> channelCurrent;
    private Integer frequency;
}
