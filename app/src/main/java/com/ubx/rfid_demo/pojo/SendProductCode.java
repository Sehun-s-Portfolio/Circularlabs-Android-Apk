package com.ubx.rfid_demo.pojo;

public class SendProductCode {
    private String productCode;
    private String productSerialCode;

    public SendProductCode(){

    }

    public SendProductCode(String productCode, String productSericalCode){
        this.productCode = productCode;
        this.productSerialCode = productSericalCode;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductSerialCode() {
        return productSerialCode;
    }

    public void setProductSerialCode(String productSerialCode) {
        this.productSerialCode = productSerialCode;
    }
}
