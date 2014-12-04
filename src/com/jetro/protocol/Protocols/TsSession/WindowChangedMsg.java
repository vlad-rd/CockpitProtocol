package com.jetro.protocol.Protocols.TsSession;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.jetro.protocol.Core.BaseMsg;
import com.jetro.protocol.Core.ClassID;
import com.jetro.protocol.Core.MESSAGE_CATEGORY;


public class WindowChangedMsg extends BaseMsg {
	
	public int HWND = 0;
    
    public String Title = "";
    
    public float Zoom = 1.0F;
    
    public float ScrlX= 1.0F;
    
    public float ScrlY= 1.0F;

	@Override
	public void initialize() {
		msgCategory = MESSAGE_CATEGORY.MOBILE_TS.ValueOf();
		msgCalssID = ClassID.WindowChangedMsg.ValueOf();

	}

	@Override
	public String serializeJson() {
		JSONObject objectJson = new JSONObject();
		try
		{
			objectJson.put("MsgID", MsgId);
			objectJson.put("HWND", HWND);
			objectJson.put("Title", Title);
			objectJson.put("Zoom", Zoom);
			objectJson.put("ScrlX", ScrlX);
			objectJson.put("ScrlY", ScrlY);
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
			HWND = Integer.valueOf(jsonObject.get("HWND").toString());
			System.out.println(HWND);
			Title = (String)jsonObject.get("Title");
			Zoom = Float.valueOf(jsonObject.get("Zoom").toString());
			ScrlX = Float.valueOf(jsonObject.get("ScrlX").toString());
			ScrlY = Float.valueOf(jsonObject.get("ScrlY").toString());
			System.out.println(Title + " " + ScrlX + " " + ScrlY);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
