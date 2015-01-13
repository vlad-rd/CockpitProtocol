package com.jetro.protocol.Protocols.TsSession;

import java.io.UnsupportedEncodingException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.jetro.protocol.Core.BaseMsg;
import com.jetro.protocol.Core.ClassID;
import com.jetro.protocol.Core.MESSAGE_CATEGORY;
import com.jetro.protocol.Core.ReadWriteOperators;


public class WindowCreatedMsg extends BaseMsg {

	public Window Task  = new Window();
	@Override
	public void initialize() {
		msgCategory = MESSAGE_CATEGORY.MOBILE_TS.ValueOf();
		msgCalssID = ClassID.WindowCreatedMsg.ValueOf();
		SerializationMethod = 1;
	}

	@Override
	public String serializeJson() {
		JSONObject oJson = new JSONObject();
		try
		{
			oJson.put("MsgID", MsgId);
			if (Task != null)
	        {
	            oJson.put("Task", Task);
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
			JSONObject cTask = (JSONObject) jsonObject.get("Task");
			Task.Title = (String) cTask.get("Title");
			Task.HWND = Integer.valueOf(cTask.get("HWND").toString());
			Task.PID = Integer.valueOf(cTask.get("PID").toString());
			Task.AppID = (String) cTask.get("AppID");
			Task.HParent = Integer.valueOf(cTask.get("HParent").toString());
			
			JSONArray icons = (JSONArray) cTask.get("Icon");
			Task.Icon = new byte[icons.size()];
			for(int k=0; k< icons.size(); k++){
				Task.Icon[k] = Long.valueOf(icons.get(k).toString()).byteValue();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public void packBin()
	{
		if(Task.Icon == null) Task.Icon = new byte[0];
		byte[] mid = new byte[0];
		byte[] appID = new byte[0];
        byte[] title = new byte[0];
		try {
			mid = MsgId.getBytes("UTF-8");
			appID = Task.AppID.getBytes("UTF-8");
			title = Task.Title.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Data = new byte[4 + mid.length + 4 + 4 + 4 + 4 + appID.length+ 4 + title.length + 4 + Task.Icon.length];
        ReadWriteOperators rwo = new ReadWriteOperators();
        rwo.WriteBinary(mid, Data);
        rwo.WriteInt(Task.HWND, Data);
        rwo.WriteInt(Task.HParent, Data);
        rwo.WriteInt(Task.PID, Data);
        rwo.WriteBinary(appID, Data);
        rwo.WriteBinary(title, Data);
        rwo.WriteBinary(Task.Icon, Data);
 	}
	
	public void unpackBin(int offset, byte[] buffer)
	{
		initialize();
		ReadWriteOperators rwo = new ReadWriteOperators();
		rwo.offset = offset;
		byte[] mid = rwo.ReadBinary(buffer);
		try {
			MsgId = new String(mid, 0, mid.length, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Task = new Window();
		Task.HWND = rwo.ReadInt(buffer);
		Task.HParent = rwo.ReadInt(buffer);
		Task.PID = rwo.ReadInt(buffer);
		
		byte[] appId = rwo.ReadBinary(buffer);
		byte[] title = rwo.ReadBinary(buffer);
		try {
			Task.AppID = new String(appId, 0, appId.length, "UTF-8");
			Task.Title = new String(title, 0, title.length, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Task.Icon = rwo.ReadBinary(buffer);
	}
}
