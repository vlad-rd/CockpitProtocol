package com.jetro.protocol.Protocols.Controller;

import java.io.Serializable;

import org.json.simple.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

// TODO: check if the Parcelable interface is use, if not delete it
public class ConnectionPoint implements Serializable, Parcelable {
	
	private static final long serialVersionUID = 7368317124217687402L;
	
	public String IP = "";
    public int Port = 0;
    public boolean WAN;
    public boolean SSL = false;
    
    public ConnectionPoint() {
    }
    
    public ConnectionPoint(Parcel in) {
    	readFromParcel(in);
    }
    
    public String getIP() {
		return IP;
	}

	public void setIP(String iP) {
		IP = iP;
	}

	public int getPort() {
		return Port;
	}

	public void setPort(int port) {
		Port = port;
	}

	public boolean isWAN() {
		return WAN;
	}

	public void setWAN(boolean wAN) {
		WAN = wAN;
	}

	public boolean isSSL() {
		return SSL;
	}

	public void setSSL(boolean sSL) {
		SSL = sSL;
	}

	@Override
    public String toString()
    {
    	JSONObject objJson = new JSONObject();
		try
		{
			objJson.put("IP", IP);
			objJson.put("Port", Port);
			objJson.put("WAN", WAN);
			objJson.put("SSL", SSL);
			return objJson.toJSONString();
		}
		catch(Exception e)
		{
			return "";
		}
    }

	@Override
	public int describeContents() {
		return hashCode();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(IP);
		dest.writeInt(Port);
		dest.writeByte((byte) ((WAN) ? 1 : 0));
		dest.writeByte((byte) ((SSL) ? 1 : 0));
	}
	
	public void readFromParcel(Parcel source) {
		IP = source.readString();
		Port = source.readInt();
		WAN = source.readByte() != 0;
		SSL = source.readByte() != 0;
	}
	
	public static final Creator<ConnectionPoint> CREATOR = new Creator<ConnectionPoint>() {

		@Override
		public ConnectionPoint createFromParcel(Parcel source) {
			return new ConnectionPoint(source);
		}

		@Override
		public ConnectionPoint[] newArray(int size) {
			return new ConnectionPoint[size];
		}
	};

	@Override
	public boolean equals(Object other) {
		if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof ConnectionPoint)) {
        	return false;
        }
        ConnectionPoint connectionPoint = (ConnectionPoint) other;
		return IP.equals(connectionPoint.IP) && Port == connectionPoint.Port;
	}
	
}
