package com.vincent.acnt.entity;

public enum RegisterProvider {
    EMAIL("Email"), FACEBOOK("Facebook"), GOOGLE("Google");

    private String provider;

    RegisterProvider(String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }
}
