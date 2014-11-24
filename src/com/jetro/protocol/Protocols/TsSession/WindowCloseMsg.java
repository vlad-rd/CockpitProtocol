package com.jetro.protocol.Protocols.TsSession;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.jetro.protocol.Core.BaseMsg;
import com.jetro.protocol.Core.ClassID;
import com.jetro.protocol.Core.MESSAGE_CATEGORY;


public class WindowCloseMsg extends BaseMsg {

	public int HWND = 0;
	
	@Override
	public void initialize() {
		msgCategory = MESSAGE_CATEGORY.MOBILE_TS.ValueOf();
		msgCalssID = ClassID.WindowCloseMsg.ValueOf();
	}

	@Override
	public String serializeJson() {
		JSONObject objectJson = new JSONObject();
		try
		{
			objectJson.put("MsgID", MsgId);
			objectJson.put("HWND", HWND);
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
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
