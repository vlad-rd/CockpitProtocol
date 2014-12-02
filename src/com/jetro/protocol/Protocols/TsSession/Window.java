package com.jetro.protocol.Protocols.TsSession;

import org.json.simple.JSONObject;

public class Window {

    public int HWND = 0;
    
    public int HParent = 0;
    
    public int PID = 0;
       
    public String Title = "";
    
    public byte[] Icon = new byte[0];
    
    public String AppID = "";
    
    public float ZoomFactor = 1.0f;
    
    public int ScrollX = 0;
    
    public int ScrollY = 0;
    
    @Override
	public int hashCode() {
		return this.HWND;
	}

	@Override
	public boolean equals(Object other) {
    	if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof Window)) {
        	return false;
        }
        Window task = (Window) other;
        return this.HWND == task.HWND;
	}

	@Override
    public String toString()
    {
    	JSONObject objJson = new JSONObject();
		try
		{
			objJson.put("HWND", HWND);
			objJson.put("HParent", HParent);
			objJson.put("PID", PID);
			objJson.put("Title", Title);
			objJson.put("AppID", AppID);
			objJson.put("ZoomFactor", ZoomFactor);
			objJson.put("ScrollX", ScrollX);
			objJson.put("ScrollY", ScrollY);
			return objJson.toJSONString();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return "";
		}
    }

}
