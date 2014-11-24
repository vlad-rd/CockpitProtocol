package com.jetro.protocol.Protocols.TsSession;

import java.io.UnsupportedEncodingException;
import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.jetro.protocol.Core.BaseMsg;
import com.jetro.protocol.Core.ClassID;
import com.jetro.protocol.Core.MESSAGE_CATEGORY;
import com.jetro.protocol.Core.ReadWriteOperators;


public class ShowTaskListMsg extends BaseMsg {
	
    public int ActiveHWND = 0;
    
    public Window[] Tasks = new Window[0];
    
	@Override
	public void initialize() {
		msgCategory = MESSAGE_CATEGORY.MOBILE_TS.ValueOf();
		msgCalssID = ClassID.ShowTaskListMsg.ValueOf();
		SerializationMethod = 1;
	}

	@Override
	public String serializeJson() {
		JSONObject oJson = new JSONObject();
		JSONArray list = new JSONArray();
		try
		{
			oJson.put("MsgID", MsgId);
			oJson.put("ActiveHWND", ActiveHWND);
			if (Tasks != null)
	        {
	            for(Window w : Tasks) 
	            {
	            	list.add(w);
	            }
	            oJson.put("Tasks", list);
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
			ActiveHWND = Integer.valueOf(jsonObject.get("ActiveHWND").toString());
			System.out.println(ActiveHWND);
			
			org.json.simple.JSONArray cTasks = (org.json.simple.JSONArray) jsonObject.get("Tasks");
			Tasks = new Window[cTasks.size()];
			for(int i=0; i< cTasks.size(); i++){
				Tasks[i] = new Window();
				JSONObject jcp = (JSONObject) cTasks.get(i);
				Tasks[i].HWND = Integer.valueOf(jcp.get("HWND").toString());
				Tasks[i].HParent = Integer.valueOf(jcp.get("HParent").toString());
				Tasks[i].PID = Integer.valueOf(jcp.get("PID").toString());
				Tasks[i].AppID = (String) jcp.get("AppID");
				Tasks[i].Title = (String) jcp.get("Title");
				JSONArray icons = (JSONArray) jcp.get("Icon");
				Tasks[i].Icon = new byte[icons.size()];
				for(int k=0; k< icons.size(); k++){
					Tasks[i].Icon[k] = Long.valueOf(icons.get(k).toString()).byteValue();
				}
				System.out.println(Tasks[i].toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public void packBin()
	{
		byte[] mid = new byte[0];
		try {
			mid = MsgId.getBytes("UTF-8");

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		Vector<byte[]> apps = new Vector<byte[]>();
        Vector<byte[]> titles = new Vector<byte[]>();
        
        int size = 4 + mid.length + 4 + 2;
        
        for(int i = 0; i < Tasks.length; i++)
        {
        	if (Tasks[i].Icon == null) Tasks[i].Icon = new byte[0];
        	byte[] appID = new byte[0];
			byte[] title = new byte[0];
			try {
				appID = Tasks[i].AppID.getBytes("UTF-8");
				title = Tasks[i].Title.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			titles.add(title);
            size += 12 + 4 + appID.length + 4 + title.length + 4 + Tasks[i].Icon.length;
        }
		
        Data = new byte[size];
        ReadWriteOperators rwo = new ReadWriteOperators();
        rwo.WriteBinary(mid, Data);
        rwo.WriteInt(ActiveHWND, Data);
        rwo.WriteShort(((Integer)Tasks.length).shortValue(), Data);
       
        for(int i = 0; i < Tasks.length; i++)
        {
        	rwo.WriteInt(Tasks[i].HWND, Data);
        	rwo.WriteInt(Tasks[i].HParent, Data);
        	rwo.WriteInt(Tasks[i].PID, Data);
        	rwo.WriteBinary(apps.get(i), Data);
        	rwo.WriteBinary(titles.get(i), Data);
        	rwo.WriteBinary(Tasks[i].Icon, Data);
        }
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
		ActiveHWND = rwo.ReadInt(buffer);
		Short nSize = rwo.ReadShort(buffer);
		Tasks = new Window[nSize];
		for(int i = 0; i < nSize; i++)
		{
			Tasks[i] = new Window();
			Tasks[i].HWND = rwo.ReadInt(buffer);
			Tasks[i].HParent = rwo.ReadInt(buffer);
			Tasks[i].PID = rwo.ReadInt(buffer);
			byte[] appID = rwo.ReadBinary(buffer);
			byte[] title = rwo.ReadBinary(buffer);
			try {
				Tasks[i].AppID = new String(appID, 0, appID.length, "UTF-8");
				Tasks[i].Title = new String(title, 0, title.length, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Tasks[i].Icon = rwo.ReadBinary(buffer);
		}
	}

}
