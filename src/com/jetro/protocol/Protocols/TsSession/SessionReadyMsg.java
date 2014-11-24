package com.jetro.protocol.Protocols.TsSession;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.jetro.protocol.Core.BaseMsg;
import com.jetro.protocol.Core.ClassID;
import com.jetro.protocol.Core.MESSAGE_CATEGORY;


public class SessionReadyMsg extends BaseMsg {
	
	//out
    public int SessionID = 0;
    
    public String CockpitSessionID = "";
    
    public String Username = "";
    
    public String DomainName = "";
  
	@Override
	public void initialize() {
		msgCategory = MESSAGE_CATEGORY.MOBILE_TS.ValueOf();
		msgCalssID = ClassID.SessionReadyMsg.ValueOf();
	}

	@Override
	public String serializeJson() {
		JSONObject objectJson = new JSONObject();
		try
		{
			objectJson.put("MsgID", MsgId);
			objectJson.put("SessionID", SessionID);
			objectJson.put("CockpitSessionID", CockpitSessionID);
			objectJson.put("Username", Username);
			objectJson.put("DomainName", DomainName);
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
			SessionID = Integer.valueOf(jsonObject.get("SessionID").toString());
			System.out.println(SessionID);
			CockpitSessionID = (String) jsonObject.get("CockpitSessionID");
			System.out.println(CockpitSessionID);
			Username = (String) jsonObject.get("Username");
			System.out.println(Username);
			DomainName = (String) jsonObject.get("DomainName");
			System.out.println(DomainName);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
