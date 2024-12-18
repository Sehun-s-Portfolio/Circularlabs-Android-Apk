package com.ubx.rfid_demo.pojo;


import java.util.List;

public class OutScanData {
    private String machineId;
    private String outTag;
    private String selectClientCode;
    private String supplierCode;
    private List<SendProductCode> productCodes;


    public OutScanData(){

    }

    public OutScanData(String machineId, String outTag, String selectClientCode, String supplierCode, List<SendProductCode> productCodes){
        this.machineId = machineId;
        this.outTag = outTag;
        this.selectClientCode = selectClientCode;
        this.supplierCode = supplierCode;
        this.productCodes = productCodes;
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public String getOutTag() {
        return outTag;
    }

    public void setOutTag(String outTag) {
        this.outTag = outTag;
    }

    public String getSelectClientCode() {
        return selectClientCode;
    }

    public void setSelectClientCode(String selectClientCode) {
        this.selectClientCode = selectClientCode;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    public List<SendProductCode> getProductCodes() {
        return productCodes;
    }

    public void setProductCodes(List<SendProductCode> productCodes) {
        this.productCodes = productCodes;
    }

    public OutScanData getOutScanData(){
        return new OutScanData(getMachineId(), getOutTag(), getSelectClientCode(), getSupplierCode(), getProductCodes());
    }
}

