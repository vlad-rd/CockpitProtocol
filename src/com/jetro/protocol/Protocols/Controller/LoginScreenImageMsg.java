package com.jetro.protocol.Protocols.Controller;

import java.io.UnsupportedEncodingException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.jetro.protocol.Core.BaseMsg;
import com.jetro.protocol.Core.ClassID;
import com.jetro.protocol.Core.MESSAGE_CATEGORY;
import com.jetro.protocol.Core.ReadWriteOperators;


public class LoginScreenImageMsg extends BaseMsg {
	//in
    public String ImageName = "";
    //out
    public byte[] Image = new byte[0];
    
	@Override
	public void initialize() {
		SerializationMethod = 1;
		msgCategory = MESSAGE_CATEGORY.MOBILE_CONTROLLER.ValueOf();
		msgCalssID = ClassID.LoginScreenImageMsg.ValueOf();
	}

	@Override
	public String serializeJson() {
		JSONObject objJson = new JSONObject();
		try
		{
			objJson.put("MsgId", MsgId);
			objJson.put("ImageName", ImageName);
			org.json.JSONArray list = new org.json.JSONArray(Image);
			objJson.put("Image", list);
			//objJson.put("Icon",Base64.getEncoder().encodeToString(Icon));
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
		initialize();
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(s);
			JSONObject jsonObject = (JSONObject) obj;
			MsgId = (String) jsonObject.get("MsgID");
			ImageName = (String) jsonObject.get("ImageName");
			JSONArray bytes = (JSONArray) jsonObject.get("Image");
			Image = new byte[bytes.size()];
			for(int k=0; k< bytes.size(); k++){
				Object o = bytes.get(k);
				Image[k] = Long.valueOf(o.toString()).byteValue();
			}
		} catch (Exception e) {
		e.printStackTrace();
		}
	}
	
	public void packBin()
	{
		if(Image == null) Image = new byte[0];
		byte[] mid = new byte[0];
        byte[] name = new byte[0];
		try {
			mid = MsgId.getBytes("UTF-8");
			name = ImageName.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Data = new byte[4 + mid.length + 4 + name.length + 4 + Image.length];
        ReadWriteOperators rwo = new ReadWriteOperators();
        rwo.WriteBinary(mid, Data);
        rwo.WriteBinary(name, Data);
        rwo.WriteBinary(Image, Data);
 	}
	
	public void unpackBin(int offset, byte[] buffer)
	{
		initialize();
		ReadWriteOperators rwo = new ReadWriteOperators();
		rwo.offset = offset;
		byte[] mid = rwo.ReadBinary(buffer);
		byte[] name = rwo.ReadBinary(buffer);
		try {
			MsgId = new String(mid, 0, mid.length, "UTF-8");
			ImageName = new String(name, 0, name.length, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Image = rwo.ReadBinary(buffer);
	}
}
