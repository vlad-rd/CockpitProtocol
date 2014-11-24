package com.jetro.protocol.Protocols.TsSession;

import org.json.JSONArray;
import org.json.simple.JSONObject;

public class Window {

    public int HWND = 0;
    
    public int HParent = 0;
    
    public int PID = 0;
       
    public String Title = "";
    
    public byte[] Icon = new byte[0];
    
    public String AppID = "";
    
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
			JSONArray list = new JSONArray(Icon);
			objJson.put("Icon", list);
			objJson.put("AppID", AppID);
			return objJson.toJSONString();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return "";
		}
    }

}
