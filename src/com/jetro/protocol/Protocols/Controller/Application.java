package com.jetro.protocol.Protocols.Controller;

//import java.util.Base64;
//import java.util.Base64.Encoder;

import org.json.simple.JSONObject;


public class Application {
	
    public String ID = "";
  
    public String Name = "";
    
    public String CommandLine = "";
    
    public String Arguments = "";
    
    public byte[] Icon = null;
    
    public boolean IsActive;
    
    @Override
    public String toString()
    {
    	JSONObject objJson = new JSONObject();
		try
		{
			objJson.put("ID", ID);
			objJson.put("Name", Name);
			objJson.put("CommandLine", CommandLine);
			objJson.put("Arguments", Arguments);
//			JSONArray list = new JSONArray(Icon);
//			objJson.put("Icon", list);
			objJson.put("IsActive", false);
			return objJson.toJSONString();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return "";
		}
    }
 
}
