package com.burbon13.kriptdapick;

public class ImageSavedDTO {
    private String data;
    private int returnCode;

    ImageSavedDTO(String data, int returnCode) {
        this.data = data;
        this.returnCode = returnCode;
    }

    public String getData() {
        return data;
    }

    public int getReturnCode() {
        return returnCode;
    }
}
