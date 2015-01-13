package com.jetro.protocol.Protocols.Controller;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.jetro.protocol.Core.BaseMsg;
import com.jetro.protocol.Core.ClassID;
import com.jetro.protocol.Core.MESSAGE_CATEGORY;


public class GetTsMsg extends BaseMsg {
	

    public String Ticket = "";
    
    public String Address = "";
    
    public int Port = 0;
    
    public String CommandLine = "";
    
	@Override
	public void initialize() {
		msgCategory = MESSAGE_CATEGORY.MOBILE_CONTROLLER.ValueOf();
		msgCalssID = ClassID.GetTsMsg.ValueOf();
	}

	@Override
	public String serializeJson() {
		JSONObject objectJson = new JSONObject();
		try
		{
			objectJson.put("MsgID", MsgId);
			objectJson.put("Address", Address);
			objectJson.put("Port", Port);
			objectJson.put("Ticket", Ticket);
			objectJson.put("CommandLine", CommandLine);
		}
		catch(Exception e)
		{
			return null;
		}
		
		return objectJson.toString();
	}

	@Override
	public void deserializeJson(String s) {
		try {
			msgCategory = MESSAGE_CATEGORY.MOBILE_CONTROLLER.ValueOf();
			msgCalssID = ClassID.GetTsMsg.ValueOf();
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(s);
			JSONObject jsonObject = (JSONObject) obj;
			MsgId = (String) jsonObject.get("MsgID");
			System.out.println(MsgId);
			Address = (String) jsonObject.get("Address");
			System.out.println(Address);
			Port = Integer.valueOf(jsonObject.get("Port").toString());
			Ticket = (String) jsonObject.get("Ticket");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
