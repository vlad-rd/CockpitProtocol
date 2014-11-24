package com.jetro.protocol.Core;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class ReadWriteOperators {

	public int offset = 0;

	public ReadWriteOperators() {
		offset = 0;
	}

	public  void WriteInt(int data, byte[] buf) {
		ByteBuffer.wrap(buf).putInt(offset, data);
		offset += 4;
	}

	public  void WriteByte(byte data, byte[] buf) {
		ByteBuffer.wrap(buf).put(offset, data);
		offset += 1;
	}

	public  void WriteShort(short data, byte[] buf) {
		ByteBuffer.wrap(buf).putShort(offset, data);
		offset += 2;
	}

	public  void WriteBinary(byte[] data, byte[] buf) {
		WriteInt(data.length,buf);
		System.arraycopy(data, 0, buf, offset, data.length);
		offset += data.length;
	}

	public  int ReadInt(byte[] buf) {
		int result = ByteBuffer.wrap(buf).getInt(offset);
		offset += 4;
		return result;
	}

	public  short ReadShort(byte[] buf) {
		short result = ByteBuffer.wrap(buf).getShort(offset);
		offset += 2;
		return result;
	}

	public  byte ReadByte(byte[] buf) {
		byte result = ByteBuffer.wrap(buf).get(offset);
		offset += 1;
		return result;
	}

	public  String ReadString(byte[] buf) {
		byte b = ReadByte(buf);
		String s = null;
		try {
			s = new String(buf, offset, (int) b, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		offset += (int) b;
		return s;
	}

	public  byte[] ReadBinary(byte[] buf) {
		int l = ReadInt(buf);
		byte[] result = new byte[l];
		System.arraycopy(buf, offset, result, 0, l);
		offset += (int) l;
		return result;
	}
}

