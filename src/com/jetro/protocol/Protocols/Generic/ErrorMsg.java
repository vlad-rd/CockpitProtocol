package com.jetro.protocol.Protocols.Generic;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.jetro.protocol.Core.BaseMsg;
import com.jetro.protocol.Core.ClassID;
import com.jetro.protocol.Core.MESSAGE_CATEGORY;


public class ErrorMsg extends BaseMsg {
	
	public static final int ERROR_INVALID_USER_CREDENTIALS = 112;
	public static final int ERROR_PASSWORD_CHANGE_FAILURE = 155;
	public static final int ERROR_NONE_AVAILABLE_TS = 105;
	public static final int ERROR_START_APPLICATION_FAILURE = 121;
	public static final int ERROR_INVALID_TICKET = 104;
	public static final int ERROR_TIMEOUT = 5;
	public static final int ERROR_UNEXPECTED = 999;
	
	public int Err = 0;
    
    public String Description = "";
    
    public ErrorMsg() {
    	initialize();
    }

	public ErrorMsg(String msgId, int err, String description) {
		initialize();
		MsgId = msgId;
		Err = err;
		Description = description;
	}

	@Override
	public void initialize() {
		msgCategory = MESSAGE_CATEGORY.GENERIC.ValueOf();
		msgCalssID = ClassID.Error.ValueOf();

	}

	@Override
	public String serializeJson() {
		JSONObject objectJson = new JSONObject();
		try
		{
			objectJson.put("MsgID", MsgId);
			objectJson.put("Err", Err);
			objectJson.put("Description", Description);
		}
		catch(Exception e)
		{
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
			Err = Integer.valueOf(jsonObject.get("Err").toString());
			System.out.println(Err);
			Description = (String) jsonObject.get("Description");
			System.out.println(Description);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString()
	{
		return "Err = " + Err + " Description:" + Description;
	}

}
