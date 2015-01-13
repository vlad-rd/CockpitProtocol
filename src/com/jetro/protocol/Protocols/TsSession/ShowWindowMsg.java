package com.jetro.protocol.Protocols.TsSession;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.jetro.protocol.Core.BaseMsg;
import com.jetro.protocol.Core.ClassID;
import com.jetro.protocol.Core.MESSAGE_CATEGORY;


public class ShowWindowMsg extends BaseMsg {
	
	public int PID = 0;
    public int HWND = 0;

	@Override
	public void initialize() {
		msgCategory = MESSAGE_CATEGORY.MOBILE_TS.ValueOf();
		msgCalssID = ClassID.ShowWindowMsg.ValueOf();

	}

	@Override
	public String serializeJson() {
		JSONObject objectJson = new JSONObject();
		try
		{
			objectJson.put("MsgID", MsgId);
			objectJson.put("PID", PID);
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
			PID = Integer.valueOf(jsonObject.get("PID").toString());
			System.out.println(PID);
			HWND = Integer.valueOf(jsonObject.get("HWND").toString());
			System.out.println(HWND);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
