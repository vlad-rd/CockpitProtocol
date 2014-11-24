package com.jetro.protocol.Core;

public enum MESSAGE_CATEGORY {
	GENERIC((byte)1),
	MOBILE_CONTROLLER((byte)51),
	MOBILE_TS((byte)52);
	
	private byte value = 1;

    private MESSAGE_CATEGORY(byte category) {
            this.value = category;
    }
    
    public byte ValueOf(){
    	return value;
    }
}
