package com.jetro.protocol.Core;

public class ExtraHeader {

	public byte MajorProtolVer;
	public byte MinorProtolVer;
	public byte SourceIDLength;
	public String SourceID;
	public byte DestinationIDLength;
	public String DestinationID;
	public byte Flags;
	public byte Reserved;
	public byte MsgCategory;
	public short MsgClassID;
	public byte EncryptionMethod;
	public byte SeralizationMethod;

	public ExtraHeader() {
		// TODO Auto-generated constructor stub
	}
}
