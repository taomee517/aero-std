//package com.aero.std.common.utils.discard;
//
//import com.aero.std.common.utils.ZeroFillUtil;
//import org.apache.commons.lang3.StringUtils;
//
///**
// * @author 罗涛
// * @title VersionUtil
// * @date 2020/5/11 17:27
// */
//public class VersionUtil {
//    public static int version2Byte(String version) throws Exception{
//        if(!validateVersion(version)){
//            throw new Exception("不合法的版本号！");
//        }
//        String[] verArr = StringUtils.split(version, ".");
//        int bigVer = Integer.parseInt(verArr[0]);
//        int smallVer = Integer.parseInt(verArr[1]);
//        return  (bigVer & 0xf) << 4 | (smallVer & 0xf);
//    }
//
//    public static boolean validateVersion(String version){
//        boolean splitSign = StringUtils.countMatches(version,".") == 1;
//        if(!splitSign){
//            return splitSign;
//        }
//        String[] verArr = StringUtils.split(version, ".");
//        int bigVer = Integer.parseInt(verArr[0]);
//        int smallVer = Integer.parseInt(verArr[1]);
//        boolean bigValidate = bigVer > 0 && bigVer < 16;
//        if(!bigValidate){
//            return bigValidate;
//        }
//        boolean smallValidate = smallVer >= 0 && smallVer < 16;
//        if(!smallValidate){
//            return smallValidate;
//        }
//        return true;
//    }
//
//    public static void main(String[] args) throws Exception {
//        String veriosn = "1.12";
//        int v = version2Byte(veriosn);
//        String bv = Integer.toBinaryString(v);
//        String fbv = ZeroFillUtil.zeroFillStr(bv,8);
//        System.out.println(fbv);
//    }
//}
