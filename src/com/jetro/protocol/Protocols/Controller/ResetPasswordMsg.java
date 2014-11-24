package com.jetro.protocol.Protocols.Controller;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.jetro.protocol.Core.BaseMsg;
import com.jetro.protocol.Core.ClassID;
import com.jetro.protocol.Core.MESSAGE_CATEGORY;


public class ResetPasswordMsg extends BaseMsg {
	
	//In
    public String Name = "";
    
    public String NewPassword = "";
    
    public String OldPassword = "";
    
    public String Domain = "";
    
    //Out
    public String Ticket = "";

	@Override
	public void initialize() {
		msgCategory = MESSAGE_CATEGORY.MOBILE_CONTROLLER.ValueOf();
		msgCalssID = ClassID.ResetPasswordMsg.ValueOf();
	}

	@Override
	public String serializeJson() {
		JSONObject objectJson = new JSONObject();
		try
		{
			objectJson.put("MsgID", MsgId);
			objectJson.put("Name", Name);
			objectJson.put("OldPassword", OldPassword);
			objectJson.put("NewPassword", NewPassword);
			objectJson.put("Domain", Domain);
			objectJson.put("Ticket", Ticket);
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
			msgCalssID = ClassID.ResetPasswordMsg.ValueOf();
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(s);
			JSONObject jsonObject = (JSONObject) obj;
			MsgId = (String) jsonObject.get("MsgID");
			System.out.println(MsgId);
			Name = (String) jsonObject.get("Name");
			System.out.println(Name);
			OldPassword = (String) jsonObject.get("OldPassword");
			System.out.println(OldPassword);
			NewPassword = (String) jsonObject.get("NewPassword");
			System.out.println(OldPassword);
			Domain = (String) jsonObject.get("Domain");
			System.out.println(Domain);
			Ticket = (String) jsonObject.get("Ticket");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
