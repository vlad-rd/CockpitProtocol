package com.jetro.protocol.Core;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import com.jetro.protocol.Protocols.Controller.ApplicationIconMsg;
import com.jetro.protocol.Protocols.Controller.CockpitSiteInfoMsg;
import com.jetro.protocol.Protocols.Controller.GetTsMsg;
import com.jetro.protocol.Protocols.Controller.LoginMsg;
import com.jetro.protocol.Protocols.Controller.LoginScreenImageMsg;
import com.jetro.protocol.Protocols.Controller.LogoutMsg;
import com.jetro.protocol.Protocols.Controller.MyApplicationsMsg;
import com.jetro.protocol.Protocols.Controller.ResetPasswordMsg;
import com.jetro.protocol.Protocols.Generic.ErrorMsg;
import com.jetro.protocol.Protocols.TsSession.KillProcessMsg;
import com.jetro.protocol.Protocols.TsSession.SessionEndMsg;
import com.jetro.protocol.Protocols.TsSession.SessionReadyMsg;
import com.jetro.protocol.Protocols.TsSession.ShowKeyBoardMsg;
import com.jetro.protocol.Protocols.TsSession.ShowTaskListMsg;
import com.jetro.protocol.Protocols.TsSession.ShowWindowMsg;
import com.jetro.protocol.Protocols.TsSession.StartApplicationMsg;
import com.jetro.protocol.Protocols.TsSession.StartRdpMsg;
import com.jetro.protocol.Protocols.TsSession.WindowChangedMsg;
import com.jetro.protocol.Protocols.TsSession.WindowCloseMsg;
import com.jetro.protocol.Protocols.TsSession.WindowCreatedMsg;
import com.jetro.protocol.Protocols.TsSession.WindowDestroyedMsg;


public abstract class BaseMsg {
	// Constants
	public final static int MagicNumber = 0x34591021;
	public final static int HEADER_LENGTH_ = 12;
	public final static short EXTRAHEADER_LENGTH_MIN = 11;
	private byte Flags = 0;
	private byte Reserved = 0;
	public final static byte MAJOR_VERSION = 4;
	public final static byte MINOR_VERSION = 5;
	protected byte SerializationMethod = 2;
	private byte EncryptionMethodNone = 0;
	//private byte EncryptionMethodRSA1024 = 1;
	public static int MAX_MESSAGE_SIZE = 65536;
	private ReadWriteOperators rwo = new ReadWriteOperators();

	// Members & Properties
	public byte msgCategory;
	public short msgCalssID;
	public byte[] Data;
	public byte[] Buffer;
	public Header header;
	public ExtraHeader extraHeader;
	public String MsgId = UUID.randomUUID().toString();

	/**
	 * init each message with the correct msgCategory and msgClassID
	 */
	public abstract void initialize();

	/**
	 * build JSON object and return string representation
	 * 
	 * @return - the JSON as string to send
	 */
	public abstract String serializeJson();

	/**
	 * parse the JSON string to object
	 * 
	 * @param json
	 *            - the JSON string from server
	 */
	public abstract void deserializeJson(String s);
	
	/**
	 * prepare all data in byte buffer for sending to socket stream
	 * 
	 * @return - the byte buffer accumulated with all data as bytes
	 */
	public byte[] pack() {

		initialize();

		try {
			// parse JSON to string
			String msg = serializeJson();
			// get string as bytes
			if (msg == null) {
				msg = "";
			}
			if(SerializationMethod == 2)
			{
				Data = msg.getBytes("UTF-8");
				// whole message length
				
			}
			else
			{
				packBin();
			}
			Buffer = new byte[HEADER_LENGTH_ + EXTRAHEADER_LENGTH_MIN + 4
								+ Data.length];

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		rwo.offset = 0;

		/**
		 * Header
		 */
		rwo.WriteInt(MagicNumber, Buffer);
		// Message length
		rwo.WriteInt(Buffer.length, Buffer);
		// Extra header length
		rwo.WriteShort(EXTRAHEADER_LENGTH_MIN, Buffer);
		// Flags
		rwo.WriteByte(Flags, Buffer);
		// Reserved
		rwo.WriteByte(Reserved, Buffer);

		/**
		 * Extra header
		 */
		// Major version
		rwo.WriteByte(MAJOR_VERSION, Buffer);
		// Minor version
		rwo.WriteByte(MINOR_VERSION, Buffer);
		// Source id length - currently = 0
		rwo.WriteByte((byte) 0, Buffer);
		// Destination id length - currently = 0
		rwo.WriteByte((byte) 0, Buffer);
		// Message category
		rwo.WriteByte(msgCategory, Buffer);
		// Message class id
		rwo.WriteShort(msgCalssID, Buffer);
		// Encryption Method - currently none = 0
		rwo.WriteByte(EncryptionMethodNone, Buffer);
		// Serialization Method - currently JSON = 2
		rwo.WriteByte(SerializationMethod, Buffer);
		// Flags
		rwo.WriteByte(Flags, Buffer);
		// Reserved
		rwo.WriteByte(Reserved, Buffer);
		// Json data length
		//rwo.WriteInt(Data.length, Buffer);
		// WriteJson
		rwo.WriteBinary(Data, Buffer);

		int size = Buffer.length;
		//Logger.log(LogLevel.INFO, "Message : " + serializeJson()
		//		+ " \nMessage Length : " + size);

		return Buffer;
	}

	/**
	 * unpack all message
	 * 
	 * @param buf
	 *            - the byte buffer from socket
	 */
	public BaseMsg unpack(byte[] buf){
		rwo.offset = 0;

		if (buf.length < HEADER_LENGTH_)
			return null;

		// if headers unpacking fail, stop all
		if (!unpackHeaders(buf)) {
			return null;
		}

		if (buf.length < header.MessageLength) {
			return null;
		}
		
		if( extraHeader.SeralizationMethod == 2)
		{
			String s = new String(rwo.ReadBinary(buf));
			deserializeJson(s);
		}
		else
		{
			unpackBin(rwo.offset + 4,buf);
		}

		return this;
	}

	/**
	 * unpack response headers
	 * 
	 * @param buf
	 *            - the byte buffer from socket
	 * @return - return true if all headers are valid , false otherwise
	 */
	private boolean unpackHeaders(byte[] buf) {
		if(buf.length < HEADER_LENGTH_) return false;
		
		header = new Header();
		
		rwo.offset = 0;

		if (MagicNumber != rwo.ReadInt(buf)) {
			return false;
		}

		/**
		 * Header
		 */
		header.MessageLength = rwo.ReadInt(buf);
		header.ExtraHeaderLength = rwo.ReadShort(buf);
		header.Flags = rwo.ReadByte(buf);
		header.Reserved = rwo.ReadByte(buf);
		
		if(buf.length < HEADER_LENGTH_ + header.ExtraHeaderLength) return false;
		
		extraHeader = new ExtraHeader();

		/**
		 * Extra header
		 */
		extraHeader.MajorProtolVer = rwo.ReadByte(buf);
		extraHeader.MinorProtolVer = rwo.ReadByte(buf);
		//extraHeader.SourceIDLength = rwo.ReadByte(buf);
		extraHeader.SourceID = rwo.ReadString(buf);
		//extraHeader.DestinationIDLength = rwo.ReadByte(buf);
		extraHeader.DestinationID = rwo.ReadString(buf);
		extraHeader.MsgCategory = rwo.ReadByte(buf);
		extraHeader.MsgClassID = rwo.ReadShort(buf);
		extraHeader.EncryptionMethod = rwo.ReadByte(buf);
		extraHeader.SeralizationMethod = rwo.ReadByte(buf);
		extraHeader.Flags = rwo.ReadByte(buf);
		extraHeader.Reserved = rwo.ReadByte(buf);

		return true;
	}
	
	public static boolean UnpackHeaders(byte buf[], Header h, ExtraHeader eh)
	{
		if(buf.length < HEADER_LENGTH_) return false;

		ReadWriteOperators rwo = new ReadWriteOperators();
		
		rwo.offset = 0;

		if (MagicNumber != rwo.ReadInt(buf)) {
			return false;
		}
		
		h.MessageLength = rwo.ReadInt(buf);
		h.ExtraHeaderLength = rwo.ReadShort(buf);
		h.Flags = rwo.ReadByte(buf);
		h.Reserved = rwo.ReadByte(buf);
		
		eh.MajorProtolVer = rwo.ReadByte(buf);
		eh.MinorProtolVer = rwo.ReadByte(buf);
		//extraHeader.SourceIDLength = rwo.ReadByte(buf);
		eh.SourceID = rwo.ReadString(buf);
		//extraHeader.DestinationIDLength = rwo.ReadByte(buf);
		eh.DestinationID = rwo.ReadString(buf);
		eh.MsgCategory = rwo.ReadByte(buf);
		eh.MsgClassID = rwo.ReadShort(buf);
		eh.EncryptionMethod = rwo.ReadByte(buf);
		eh.SeralizationMethod = rwo.ReadByte(buf);
		eh.Flags = rwo.ReadByte(buf);
		eh.Reserved = rwo.ReadByte(buf);
		
		return true;
	}
	
	
	public void save2File(String folder,String filename)
	{
		byte[] data = pack();
		try {
			File myFile = new File(folder + "//" + filename);
			myFile.createNewFile();
			FileOutputStream fOut = new FileOutputStream(myFile);
			fOut.write(data);
			fOut.flush();
			fOut.close();
		} catch (FileNotFoundException e) {
		    String errMsg = e.getMessage();
		    System.console().writer().print(errMsg);
		} catch (IOException e) {
			 String errMsg = e.getMessage();
			 System.console().writer().print(errMsg);
		}
	}
	
	public BaseMsg readFromFile(String folder,String filename)
	{
		try {
			File myFile = new File(folder + "//" + filename);
			byte[] buffer = new byte[(int)myFile.length()];
			FileInputStream fIn = new FileInputStream(myFile);
			fIn.read(buffer);
			fIn.close();
			return unpack(buffer);
		} catch (FileNotFoundException e) {
		    String errMsg = e.getMessage();
		    System.console().writer().print(errMsg);
		    return null;
		} catch (IOException e) {
			 String errMsg = e.getMessage();
			 System.console().writer().print(errMsg);
			 return null;
		}
	}
	
	public static BaseMsg createMessage(byte[] buffer) {
		
		Header h = new Header();
		ExtraHeader eh = new ExtraHeader();
		if(!BaseMsg.UnpackHeaders(buffer, h, eh))
			return null;
		
		ClassID classID = ClassID.GetID(eh.MsgClassID);
		
		if(eh.MsgCategory == 1)
		{
			switch (classID) {
			case Error:
				ErrorMsg err = new ErrorMsg();
				return err.unpack(buffer);
			default:
				break;
			}
		}
		
		else if(eh.MsgCategory == 51)
		{
			switch (classID) {
			case LoginMsg:
				LoginMsg lm = new LoginMsg();
				return lm.unpack(buffer);
			case CockpitSiteInfoMsg:
				CockpitSiteInfoMsg cm = new CockpitSiteInfoMsg();
				return cm.unpack(buffer);
			case MyApplicationsMsg:
				MyApplicationsMsg ma = new MyApplicationsMsg();
				return ma.unpack(buffer);
			case ResetPasswordMsg:
				ResetPasswordMsg rp = new ResetPasswordMsg();
				return rp.unpack(buffer);
			case GetTsMsg:
				GetTsMsg gt = new GetTsMsg();
				return gt.unpack(buffer);
			case LogoutMsg:
				LogoutMsg lo = new LogoutMsg();
				return lo.unpack(buffer);
			case LoginScreenImageMsg:
				LoginScreenImageMsg loi = new LoginScreenImageMsg();
				return loi.unpack(buffer);
			case ApplicationIconMsg:
				ApplicationIconMsg ai = new ApplicationIconMsg();
				return ai.unpack(buffer);
			default:
				break;
			}
		}
		else if(eh.MsgCategory == 52)
		{
			switch (classID) {
			case KillProcessMsg:
				KillProcessMsg kp = new KillProcessMsg();
				return kp.unpack(buffer);
			case SessionEndMsg:
				SessionEndMsg se = new SessionEndMsg();
				return se.unpack(buffer);
			case SessionReadyMsg:
				SessionReadyMsg sr = new SessionReadyMsg();
				return sr.unpack(buffer);
			case ShowKeyBoardMsg:
				ShowKeyBoardMsg sk = new ShowKeyBoardMsg();
				return sk.unpack(buffer);
			case ShowTaskListMsg:
				ShowTaskListMsg st = new ShowTaskListMsg();
				return st.unpack(buffer);
			case ShowWindowMsg:
				ShowWindowMsg sw = new ShowWindowMsg();
				return sw.unpack(buffer);
			case StartApplicationMsg:
				StartApplicationMsg sa = new StartApplicationMsg();
				return sa.unpack(buffer);
			case WindowChangedMsg:
				WindowChangedMsg wc = new WindowChangedMsg();
				return wc.unpack(buffer);
			case WindowCloseMsg:
				WindowCloseMsg wcl = new WindowCloseMsg();
				return wcl.unpack(buffer);
			case WindowCreatedMsg:
				WindowCreatedMsg wcr = new WindowCreatedMsg();
				return wcr.unpack(buffer);
			case WindowDestroyedMsg:
				WindowDestroyedMsg wd = new WindowDestroyedMsg();
				return wd.unpack(buffer);
			case StartRdpMsg:
				StartRdpMsg srdp = new StartRdpMsg();
				return srdp.unpack(buffer);
				
				default:
					break;
			}
		}
		return null; //TODO
	}
	
	public void packBin()
	{
		
	}
	
	public void unpackBin( int offset,byte[] buffer)
	{
		
	}
}
