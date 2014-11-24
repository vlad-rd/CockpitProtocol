package com.jetro.protocol.Protocols.TsSession;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.jetro.protocol.Core.BaseMsg;
import com.jetro.protocol.Core.ClassID;
import com.jetro.protocol.Core.MESSAGE_CATEGORY;


public class SessionEndMsg extends BaseMsg {
	
	public boolean Wait = false;

	@Override
	public void initialize() {
		msgCategory = MESSAGE_CATEGORY.MOBILE_TS.ValueOf();
		msgCalssID = ClassID.SessionEndMsg.ValueOf();

	}

	@Override
	public String serializeJson() {
		JSONObject objectJson = new JSONObject();
		try
		{
			objectJson.put("MsgID", MsgId);
			objectJson.put("Wait", Wait);
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
			Wait = Boolean.valueOf(jsonObject.get("Wait").toString());
			System.out.println(Wait);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
