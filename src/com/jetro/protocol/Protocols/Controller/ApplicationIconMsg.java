package com.jetro.protocol.Protocols.Controller;

import java.io.UnsupportedEncodingException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.jetro.protocol.Core.BaseMsg;
import com.jetro.protocol.Core.ClassID;
import com.jetro.protocol.Core.MESSAGE_CATEGORY;
import com.jetro.protocol.Core.ReadWriteOperators;


public class ApplicationIconMsg extends BaseMsg {
	//in
    public String ID = "";
    //out
    public byte[] Icon = new byte[0];
    
	@Override
	public void initialize() {
		SerializationMethod = 1;
		msgCategory = MESSAGE_CATEGORY.MOBILE_CONTROLLER.ValueOf();
		msgCalssID = ClassID.ApplicationIconMsg.ValueOf();
	}

	@Override
	public String serializeJson() {
		JSONObject objJson = new JSONObject();
		try
		{
			objJson.put("MsgId", MsgId);
			objJson.put("ID", ID);
			org.json.JSONArray list = new org.json.JSONArray(Icon);
			objJson.put("Icon", list);			//objJson.put("Icon",Base64.getEncoder().encodeToString(Icon));
			return objJson.toJSONString();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return "";
		}
	}

	@Override
	public void deserializeJson(String s) {
		try {
			msgCategory = MESSAGE_CATEGORY.MOBILE_CONTROLLER.ValueOf();
			msgCalssID = ClassID.ApplicationIconMsg.ValueOf();
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(s);
			JSONObject jsonObject = (JSONObject) obj;
			MsgId = (String) jsonObject.get("MsgID");
			ID = (String) jsonObject.get("ID");
			JSONArray bytes = (JSONArray) jsonObject.get("Icon");
			Icon = new byte[bytes.size()];
			for(int k=0; k< bytes.size(); k++){
				Object o = bytes.get(k);
				Icon[k] = Long.valueOf(o.toString()).byteValue();
			}
		} catch (Exception e) {
		e.printStackTrace();
		}
	}
	
	public void packBin()
	{
		if(Icon == null) Icon = new byte[0];
		byte[] mid = new byte[0];
        byte[] id = new byte[0];
		try {
			mid = MsgId.getBytes("UTF-8");
			id = ID.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Data = new byte[4 + mid.length + 4 + id.length + 4 + Icon.length];
        ReadWriteOperators rwo = new ReadWriteOperators();
        rwo.WriteBinary(mid, Data);
        rwo.WriteBinary(id, Data);
        rwo.WriteBinary(Icon, Data);
 	}
	
	public void unpackBin(int offset, byte[] buffer)
	{
		initialize();
		ReadWriteOperators rwo = new ReadWriteOperators();
		rwo.offset = offset;
		byte[] mid = rwo.ReadBinary(buffer);
		byte[] id = rwo.ReadBinary(buffer);

		try {
			MsgId = new String(mid, 0, id.length, "UTF-8");
			ID = new String(id, 0, id.length, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Icon = rwo.ReadBinary(buffer);
	}

}
