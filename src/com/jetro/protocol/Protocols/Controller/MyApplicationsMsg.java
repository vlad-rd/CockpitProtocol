package com.jetro.protocol.Protocols.Controller;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.jetro.protocol.Core.BaseMsg;
import com.jetro.protocol.Core.ClassID;
import com.jetro.protocol.Core.MESSAGE_CATEGORY;


public class MyApplicationsMsg extends BaseMsg {
	
	//In
    public String Ticket = "";
    
    //Out
    public Application[] Applications = null;
    
	@Override
	public void initialize() {
		msgCategory = MESSAGE_CATEGORY.MOBILE_CONTROLLER.ValueOf();
		msgCalssID = ClassID.MyApplicationsMsg.ValueOf();
	}

	@Override
	public String serializeJson() {
		JSONObject oJson = new JSONObject();
		JSONArray list = new JSONArray();
		try
		{
			oJson.put("MsgID", MsgId);
			oJson.put("Ticket", Ticket);
			if (Applications != null)
	        {
	            for(Application ap : Applications) 
	            {
	            	list.add(ap);
	            }
	            oJson.put("Applications", list);
	        }
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
		
		return oJson.toJSONString();
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
			Ticket = (String) jsonObject.get("Ticket");
			System.out.println(Ticket);
			org.json.simple.JSONArray cApps = (org.json.simple.JSONArray) jsonObject.get("Applications");
			Applications = new Application[cApps.size()];
			for(int i=0; i< cApps.size(); i++){
				Applications[i] = new Application();
				JSONObject jcp = (JSONObject) cApps.get(i);
				Applications[i].ID = (String) jcp.get("ID");
				Applications[i].Name = (String) jcp.get("Name");
				Applications[i].CommandLine = (String) jcp.get("CommandLine");
				Applications[i].Arguments = (String) jcp.get("Arguments");
				JSONArray icons = (JSONArray) jcp.get("Icon");
				Applications[i].Icon = new byte[icons.size()];
				for(int k=0; k< icons.size(); k++){
					Object o = icons.get(k);
					Applications[i].Icon[k] = Long.valueOf(o.toString()).byteValue();
				}
				System.out.println(Applications[i].toString());
			}
			System.out.println("End Parsing");
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
}
