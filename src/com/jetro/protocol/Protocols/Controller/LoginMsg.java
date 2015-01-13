package com.jetro.protocol.Protocols.Controller;


//import org.json.*;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;

import com.jetro.protocol.Core.*;


public class LoginMsg extends BaseMsg {
	
	public static final int LOGIN_FAILURE = -1;
	public static final int LOGIN_SUCCESS = 1;
	public static final int LOGIN_RESET_PASSWORD = 2;
	public static final int LOGIN_OPTIONAL_CHANGE_PASSWORD = 3;

	public String name = "";
	public String password = "";
	public String domain = "";
	public String deviceModel = "";
	public String deviceId = "";
	public String Ticket = "";
	public int returnCode = LOGIN_FAILURE;
	public int daysBeforePasswordExpiration = -1;
	public String email = "";
	
	@Override
	public void initialize() {
		msgCategory = MESSAGE_CATEGORY.MOBILE_CONTROLLER.ValueOf();
		msgCalssID = ClassID.LoginMsg.ValueOf();
	}

	@Override
	public String serializeJson() {
		JSONObject objectJson = new JSONObject();
		try
		{
			objectJson.put("MsgID", MsgId);
			objectJson.put("Name", name);
			objectJson.put("Password", password);
			objectJson.put("Domain", domain);
			objectJson.put("DeviceModel", deviceModel);
			objectJson.put("DeviceID", deviceId);
			objectJson.put("ReturnCode", new Integer(returnCode));
			objectJson.put("DaysBeforePasswordExpiration", new Integer(daysBeforePasswordExpiration));
			objectJson.put("Ticket", Ticket);
			objectJson.put("Email", email);
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
			name = (String) jsonObject.get("Name");
			System.out.println(name);
			password = (String) jsonObject.get("Password");
			System.out.println(password);
			domain = (String) jsonObject.get("Domain");
			System.out.println(domain);
			deviceModel = (String) jsonObject.get("DeviceModel");
			System.out.println(deviceModel);
			deviceId = (String) jsonObject.get("DeviceID");
			System.out.println(deviceId);
			returnCode = Integer.valueOf(jsonObject.get("ReturnCode").toString());
			System.out.println(returnCode);
			Ticket = (String) jsonObject.get("Ticket");
			System.out.println(deviceId);
			daysBeforePasswordExpiration = Integer.valueOf(jsonObject.get("DaysBeforePasswordExpiration").toString());
			System.out.println(daysBeforePasswordExpiration);
			email = (String) jsonObject.get("Email");
			System.out.println(email);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

