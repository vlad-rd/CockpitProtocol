package com.jetro.protocol.Protocols.TsSession;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.jetro.protocol.Core.BaseMsg;
import com.jetro.protocol.Core.ClassID;
import com.jetro.protocol.Core.MESSAGE_CATEGORY;


public class WindowDestroyedMsg extends BaseMsg {
	
	
    public String AppID = "";
    
    public int PID = 0;
    
    public int HWND = 0;
    
	@Override
	public void initialize() {
		msgCategory = MESSAGE_CATEGORY.MOBILE_TS.ValueOf();
		msgCalssID = ClassID.WindowDestroyedMsg.ValueOf();

	}

	@Override
	public String serializeJson() {
		JSONObject objectJson = new JSONObject();
		try
		{
			objectJson.put("MsgID", MsgId);
			objectJson.put("HWND", HWND);
			objectJson.put("PID", PID);
			objectJson.put("AppID", AppID);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
		return objectJson.toString();
	}

	@Override
	public void deserializeJson(String s) {
		initialize();
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(s);
			JSONObject jsonObject = (JSONObject) obj;
			MsgId = (String) jsonObject.get("MsgID");
			System.out.println(MsgId);
			HWND = Integer.valueOf(jsonObject.get("HWND").toString());
			System.out.println(HWND);
			PID = Integer.valueOf(jsonObject.get("PID").toString());
			System.out.println(PID);
			AppID = (String)jsonObject.get("AppID");
			System.out.println(AppID);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
