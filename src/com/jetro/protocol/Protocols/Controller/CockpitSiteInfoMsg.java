package com.jetro.protocol.Protocols.Controller;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.jetro.protocol.Core.BaseMsg;
import com.jetro.protocol.Core.ClassID;
import com.jetro.protocol.Core.MESSAGE_CATEGORY;
import com.jetro.protocol.Protocols.Controller.ConnectionPoint.ConnectionModeType;


public class CockpitSiteInfoMsg extends BaseMsg {
	
	//in
    public int RX = 0;
    public int RY = 0;
    public String ID = "";
    public String Name = "";
    public String LoginScreenText = "";
    public String LoginScreenImage = "";
    public ConnectionPoint[] ConnectionPoints = new ConnectionPoint[0];
    
    @Override
    public String toString()
    {
        String result = super.toString() + "CockpitSiteInfoMsg RX=" + RX + " RY=" + RY + " Site=" + Name + " LoginText=" + LoginScreenText + " LoginImg=" + LoginScreenImage;
        if (ConnectionPoints != null)
        {
            for(ConnectionPoint cp : ConnectionPoints) result += "\r\n" + cp.toString();
        }
        return result;
    }

    @Override
	public void initialize() {
    	msgCategory = MESSAGE_CATEGORY.MOBILE_CONTROLLER.ValueOf();
		msgCalssID = ClassID.CockpitSiteInfoMsg.ValueOf();
	}

	@Override
	public String serializeJson() {
		JSONObject oJson = new JSONObject();
		JSONArray list = new JSONArray();
		try
		{
			oJson.put("MsgID", MsgId);
			oJson.put("RX", RX);
			oJson.put("RY", RY);
			oJson.put("ID", ID);
			oJson.put("Name", Name);
			oJson.put("LoginScreenText", LoginScreenText);
			oJson.put("LoginScreenImage", LoginScreenImage);
			if (ConnectionPoints != null)
	        {
	            for(ConnectionPoint cp : ConnectionPoints) 
	            {
	            	list.add(cp);
	            }
	            oJson.put("ConnectionPoints", list);
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
		try {
			msgCategory = MESSAGE_CATEGORY.MOBILE_CONTROLLER.ValueOf();
			msgCalssID = ClassID.CockpitSiteInfoMsg.ValueOf();
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(s);
			JSONObject jsonObject = (JSONObject) obj;
			MsgId = (String) jsonObject.get("MsgID");
			System.out.println(MsgId);
			RX = Integer.valueOf(jsonObject.get("RX").toString());
			System.out.println(RX);
			RY = Integer.valueOf(jsonObject.get("RY").toString());
			System.out.println(RY);
			ID = (String) jsonObject.get("ID");
			System.out.println(ID);
			Name = (String) jsonObject.get("Name");
			System.out.println(Name);
			LoginScreenText = (String) jsonObject.get("LoginScreenText");
			System.out.println(LoginScreenText);
			LoginScreenImage = (String) jsonObject.get("LoginScreenImage");
			System.out.println(LoginScreenImage);
			// get an array from the JSON object
			org.json.simple.JSONArray cPoints = (org.json.simple.JSONArray) jsonObject.get("ConnectionPoints");
			
			ConnectionPoints = new ConnectionPoint[cPoints.size()];
			for(int i=0; i< cPoints.size(); i++){
				ConnectionPoints[i] = new ConnectionPoint();
				JSONObject jcp = (JSONObject) cPoints.get(i);
				ConnectionPoints[i].IP = (String) jcp.get("IP");
				ConnectionPoints[i].Port = Integer.valueOf(jcp.get("Port").toString());
				int connectionModeTypeIndex = Integer.valueOf(jcp.get("ConnectionMode").toString());
				ConnectionPoints[i].ConnectionMode = ConnectionModeType.values()[connectionModeTypeIndex];
				System.out.println(ConnectionPoints[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
