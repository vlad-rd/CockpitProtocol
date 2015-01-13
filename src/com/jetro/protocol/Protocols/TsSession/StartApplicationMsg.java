package com.jetro.protocol.Protocols.TsSession;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.jetro.protocol.Core.BaseMsg;
import com.jetro.protocol.Core.ClassID;
import com.jetro.protocol.Core.MESSAGE_CATEGORY;


public class StartApplicationMsg extends BaseMsg {
	
	// IN / OUT
	public String ID = "";
    
	// OUT
    public int PID = 0;

	@Override
	public void initialize() {
		msgCategory = MESSAGE_CATEGORY.MOBILE_TS.ValueOf();
		msgCalssID = ClassID.StartApplicationMsg.ValueOf();
	}

	@Override
	public String serializeJson() {
		JSONObject objectJson = new JSONObject();
		try
		{
			objectJson.put("MsgID", MsgId);
			objectJson.put("ID", ID);
			objectJson.put("PID", PID);
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
			ID = (String)jsonObject.get("ID");
			System.out.println(ID);
			PID = Integer.valueOf(jsonObject.get("PID").toString());
			System.out.println(PID);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
