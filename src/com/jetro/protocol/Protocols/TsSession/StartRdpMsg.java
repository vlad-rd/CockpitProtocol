package com.jetro.protocol.Protocols.TsSession;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.jetro.protocol.Core.*;
import com.jetro.protocol.Protocols.Controller.GetTsMsg;



public class StartRdpMsg extends BaseMsg {
	
	public String Ticket = "";
    
    public String Address = "";
    
    public int Port = 0;
    
    public StartRdpMsg()
    {
    	initialize();
    }
    
    public StartRdpMsg(GetTsMsg gts)
    {
    	initialize();
    	Address = gts.Address;
    	Port = gts.Port;
    	Ticket = gts.Ticket;
    }
    
	@Override
	public void initialize() {
		msgCategory = MESSAGE_CATEGORY.MOBILE_TS.ValueOf();
		msgCalssID = ClassID.StartRdpMsg.ValueOf();
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
			Address = (String) jsonObject.get("Address");
			System.out.println(Address);
			Port = Integer.valueOf(jsonObject.get("Port").toString());
			Ticket = (String) jsonObject.get("Ticket");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
