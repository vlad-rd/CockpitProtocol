package com.jetro.protocol.Protocols.Controller;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.jetro.protocol.Core.BaseMsg;
import com.jetro.protocol.Core.ClassID;
import com.jetro.protocol.Core.MESSAGE_CATEGORY;


public class LogoutMsg extends BaseMsg {
	
	public String Ticket = "";

	@Override
	public void initialize() {
		msgCategory = MESSAGE_CATEGORY.MOBILE_CONTROLLER.ValueOf();
		msgCalssID = ClassID.LogoutMsg.ValueOf();
	}

	@Override
	public String serializeJson() {
		JSONObject objectJson = new JSONObject();
		try
		{
			objectJson.put("Ticket", Ticket);
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
		try {
			msgCategory = MESSAGE_CATEGORY.MOBILE_CONTROLLER.ValueOf();
			msgCalssID = ClassID.LogoutMsg.ValueOf();
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(s);
			JSONObject jsonObject = (JSONObject) obj;
			MsgId = (String) jsonObject.get("MsgID");
			System.out.println(MsgId);
			Ticket = (String) jsonObject.get("Ticket");
			System.out.println(Ticket);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
