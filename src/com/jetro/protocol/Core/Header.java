package com.jetro.protocol.Core;

public class Header {

	public int MagicNumber = 0x34591021;
	public int MessageLength;
	public short ExtraHeaderLength = 11;
	public byte Flags = 0;
	public byte Reserved = 0;

	public Header() {

	}
}
