package com.ubx.rfid_demo.utils;

public class ByteUtils {

    /**
     * 将Hex String转换为Byte数组
     *
     * @param hexString the hex string
     * @return the byte [ ]
     */
    public static byte[] hexStringToBytes(String hexString) {
        hexString = hexString.toLowerCase();
        final byte[] byteArray = new byte[hexString.length() >> 1];
        int index = 0;
        for (int i = 0; i < hexString.length(); i++) {
            if (index > hexString.length() - 1) {
                return byteArray;
            }
            byte highDit = (byte) (Character.digit(hexString.charAt(index), 16) & 0xFF);
            byte lowDit = (byte) (Character.digit(hexString.charAt(index + 1), 16) & 0xFF);
            byteArray[i] = (byte) (highDit << 4 | lowDit);
            index += 2;
        }
        return byteArray;
    }


    /**
     * 获取 PC值
     * @param epc   16进制的EPC值（长度为4的倍数），例如：0123456789ABCD  、 08CD6600 、 EF89
     * @return 返回PC值为4位长度的16进制，例如：0030 、 0080 、 0101
     */
    public static String getPC(String epc){
        String pc ="0000";
        int len = epc.length()/4;//EPC除以4得到10进制的长度值
        int b = len << 11;//得到的长度值,左移11位,得到byte值
        String aHex = Integer.toHexString(b);//byte 值转成16进制,即为表示EPC长度的PC值
        if (aHex.length() == 3){
            pc = "0"+aHex;//如果byte转成16进制的长度是3位,则在前面高位补0,例如: "003" 补0后变成 "0003"
        } else {
            pc = aHex;
        }
        return pc;
    }
}
