package com.jetro.protocol.Core.Net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.jetro.protocol.Core.BaseMsg;
import com.jetro.protocol.Core.Notificator;
import com.jetro.protocol.Protocols.Generic.ErrorMsg;


public class ClientChannel extends Notificator {
	
	private static final String TAG = ClientChannel.class.getSimpleName();
	
	public static final int TIME_OUT = 2000;
	
	private static ClientChannel _Instance = null;
	
	public static ClientChannel getInstance()
	{
		return _Instance;
	}
	
	public Map<String, MessageHolder> _WaitingRequests = new HashMap<String, MessageHolder>();
	
	private SocketChannel _Socket;
	
	ByteArrayOutputStream _ByteArrayOutputStream = null;
	ByteBuffer _Chunk = null;
	
	//ClientChannel _Owner;
	
	private ClientChannel()
	{
	}
		
	public BaseMsg SendReceive(BaseMsg msg, int timeout)
	{
		try {
			MessageHolder mh = new MessageHolder();
			synchronized(_WaitingRequests)
			{
				_WaitingRequests.put(msg.MsgId, mh);
			}
			try {
				synchronized(_Socket)
				{
					_Socket.socket().getOutputStream().write( msg.pack());
				}
			} catch (IOException e) {
					Log.e(TAG, "ERROR: ", e);
					Stop();
					return new ErrorMsg(msg.MsgId, 999, "Unexpected error");
			}
			
			synchronized(mh.Event) 
			{
				mh.Event.wait(timeout);
			}
				
			BaseMsg resp = null;
			
			synchronized(_WaitingRequests)
			{
				if(_WaitingRequests.containsKey(msg.MsgId))
				{
					resp = _WaitingRequests.get(msg.MsgId).Message;
					Log.i("ClientChannel",resp.toString());
				}
			}
				
			return resp;
			
		} catch (InterruptedException e1) {
			return new ErrorMsg(msg.MsgId, 5, "Timeout error");
		}
		finally
		{
			try
			{
			synchronized(_WaitingRequests)
			{
				if(_WaitingRequests.containsKey(msg.MsgId))
								_WaitingRequests.remove(msg.MsgId);
			}
			}
			catch(Exception ex)
			{
				Log.e("ClientChannel",ex.toString());
		
			}
		}
	}
	
	public BaseMsg SendReceiveInUI(BaseMsg msg, int timeout)
	{
		try {
			MessageHolder mh = new MessageHolder();
			synchronized(_WaitingRequests)
			{
				_WaitingRequests.put(msg.MsgId, mh);
			}
			
			msgAsyncSender sender = new msgAsyncSender(msg);
			
			sender.Send();
			
			synchronized(mh.Event) 
			{
				mh.Event.wait(timeout);
			}
				
			BaseMsg resp = null;
			
			synchronized(_WaitingRequests)
			{
				if(_WaitingRequests.containsKey(msg.MsgId))
				{
					resp = _WaitingRequests.get(msg.MsgId).Message;
					Log.i("ClientChannel",resp.toString());
				}
			}
				
			return resp;
			
		} catch (InterruptedException e1) {
			return new ErrorMsg(msg.MsgId, 5, "Timeout error");
		}
		finally
		{
			try
			{
			synchronized(_WaitingRequests)
			{
				if(_WaitingRequests.containsKey(msg.MsgId))
								_WaitingRequests.remove(msg.MsgId);
			}
			}
			catch(Exception ex)
			{
				Log.e("ClientChannel",ex.toString());
		
			}
		}
	}
	
	public void SendReceiveAsync(BaseMsg msg)
	{
			msgAsyncSender sender = new msgAsyncSender(msg);
			sender.Send();
	}
	
	public void threadProc() {
		Thread.currentThread().setName("ClientChannel-Listener");
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		try {
			_Socket.socket().setSoTimeout(500);
		} catch (SocketException e) {
			Log.e(TAG, "ERROR: ", e);
			fireConnectionBroken();
			return;
		}
		
		_ByteArrayOutputStream = new ByteArrayOutputStream(40960);
		
		_Chunk = ByteBuffer.allocate(4096);
		
		int readen = 0;
		
		while(true){
			try
			{
				if(!_Socket.isConnected())
				{
					fireConnectionBroken();
					return;
				}
				_Chunk.position(0);
				readen = _Socket.read(_Chunk);
				if(readen  > 0){
					Log.i("ClientChannel","received " + readen);
					_ByteArrayOutputStream.write(_Chunk.array(),0,readen);
					while(true){
						byte[] original  = _ByteArrayOutputStream.toByteArray();
						BaseMsg msg = BaseMsg.createMessage(original);
						
						if(msg == null) break;
						
						if(msg.header.MessageLength == original.length)
						{
							_ByteArrayOutputStream.reset();
						}
						else
						{
							byte[] rest = new byte[original.length - msg.header.MessageLength];
							System.arraycopy(original, msg.header.MessageLength, rest,  0, rest.length);
							_ByteArrayOutputStream.reset();
							_ByteArrayOutputStream.write(rest);
						}
						
						if(_WaitingRequests.containsKey(msg.MsgId))
						{
							_WaitingRequests.get(msg.MsgId).Message = msg;
							synchronized(_WaitingRequests.get(msg.MsgId).Event)
							{
								_WaitingRequests.get(msg.MsgId).Event.notify();
							}
						}
						else
							fireEvent(msg);
					}
				}
			}
			catch(IOException ex)
			{
				fireConnectionBroken();
				break;
			}
		}
		Log.i("ClientChannel","threadProc exit");
    }
	
	private String _Address = "";
	private int _Port = 0;
	
	public static boolean Create(String address, int port, long timeout)
	{
		if(_Instance != null)
		{
			_Instance.Stop();
		}
		_Instance = new ClientChannel();
		
		return _Instance.create(address, port, timeout);
	}
	
	private boolean create(String address, int port, long timeout)
	{
		_Address = address;
		_Port = port;
		
		try {
			_Socket = SocketChannel.open();
		} catch (IOException e1) {
			Log.e(TAG, "ERROR: ", e1);
			_Socket = null;
			return false;
		}
			
		Thread thread = new Thread(){
			public void run(){
				Log.i("Connector","Starts");
				try {
					_Socket.connect(new InetSocketAddress(_Address, _Port));
					synchronized(_Socket)
					{
						_Socket.notify();
					}
				} catch (IOException e) {
					Log.e(TAG, "ERROR: ", e);
					synchronized(_Socket)
					{
						_Socket.notify();
					}
				}
				
			}
		  };
		  thread.start();
		  
		  Log.i("Connector","End"); 
		  
		try {
				synchronized(_Socket)
				{
					_Socket.wait(timeout);
				}
				} catch (InterruptedException e) {
				 return false;
				}
		 if( _Socket.isConnected())
		 {
			 Thread threadListener = new Thread(){
				    public void run(){
				    	threadProc();
				    }
				 };
				 threadListener.start();
			return true;
		 }
		 else
			 return false;
	}
	
	public boolean Create(String address, int port)
	{
		try
		{
			_Socket = SocketChannel.open();
			_Socket.connect(new InetSocketAddress(address, port));
			Thread thread = new Thread(){
			    public void run(){
			    	threadProc();
			    }
			 };
			 thread.start();
			 return true;
		}
		catch(IOException e)
		{
			Log.e("ClientChannel", e.toString());
			return false;
		}
	}
	
	public void Stop()
	{
		if (_Socket != null)
		{
			try {
				_Socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(_ByteArrayOutputStream != null)
		{
			try {
				_ByteArrayOutputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		_Instance = null;
	}
	
	public boolean Send(BaseMsg msg)
	{
		try
		{
			_Socket.socket().getOutputStream().write( msg.pack());
			return true;
		}
		catch(Exception ex)
		{
			Log.e(TAG, "ERROR: ", ex);
			return false;
		}
	}
	
	public void SendAsyncTimeout(BaseMsg msg,int timeout)
	{
		try
		{
			msgSenderCallBack m = new msgSenderCallBack(msg, timeout);
			m.Send();
		}
		catch(Exception ex)
		{
			Log.e(TAG, "ERROR: ", ex);
		}
	}
	
	public void SendAsync(BaseMsg msg)
	{
		try
		{
			msgAsyncSender m = new msgAsyncSender(msg);
			m.Send();
		}
		catch(Exception ex)
		{
			Log.e(TAG, "ERROR: ", ex);
		}
	}
	
	private class msgAsyncSender
	{
		BaseMsg msg;
		public msgAsyncSender(BaseMsg m)
		{
			msg = m;
		}
		public void Send()
		{
			Thread thread = new Thread(){
			    public void run(){
			    	try {
						_Socket.socket().getOutputStream().write( msg.pack());
					} catch (IOException e) {
						try {
							_Socket.close();
						} catch (IOException e1) {
							Log.e(TAG, "ERROR: ", e1);
						}
						finally
						{
							_Socket = null;
						}
						Log.e(TAG, "ERROR: ", e);
					}
			    }
			 };
			 thread.start();
		}		
	}
	
	private class msgSenderCallBack
	{
		BaseMsg msg;
		int timeout;
		public msgSenderCallBack(BaseMsg m, int tout)
		{
			msg = m;
			timeout = tout;
		}
		public void Send()
		{
			Thread thread = new Thread(){
			    public void run(){
			    	try {
			    		MessageHolder mh = new MessageHolder();
						synchronized(_WaitingRequests)
						{
							_WaitingRequests.put(msg.MsgId, mh);
						}
						
						synchronized(_Socket)
						{
							_Socket.socket().getOutputStream().write( msg.pack());
						}
						
						BaseMsg resp = null;
						
						synchronized(mh.Event) 
						{
							try {
								mh.Event.wait(timeout);
							} catch (InterruptedException e) {
								resp = new ErrorMsg(msg.MsgId, 5, "Timeout occured 1");
								fireEvent(resp);
								return;
							}
						}
						synchronized(_WaitingRequests)
						{
							if(_WaitingRequests.containsKey(msg.MsgId))
							{
								resp = _WaitingRequests.get(msg.MsgId).Message;
								if(resp == null) 
									resp = new ErrorMsg(msg.MsgId, 5, "Timeout occured for msg id = " + msg.MsgId);
								Log.i("ClientChannel",resp.toString());
							}
						}
						
						try
						{
							fireEvent(resp);
						}
						catch(Exception ex2)
						{
							Log.e(TAG, "ERROR: ", ex2);
						}
						
					} catch (IOException e) {
						try {
							_Socket.close();
						} catch (IOException e1) {
							Log.e(TAG, "ERROR: ", e1);
						}
						finally
						{
							_Socket = null;
						}
						Log.e(TAG, "ERROR: ", e);
					}
			    	finally{
			    		try
						{
						synchronized(_WaitingRequests)
						{
							if(_WaitingRequests.containsKey(msg.MsgId))
											_WaitingRequests.remove(msg.MsgId);
						}
						}
						catch(Exception ex)
						{
							Log.e("ClientChannel",ex.toString());
					
						}
			    	}
			    }
			 };
			 thread.start();
		}		
	}
	
	private class MessageHolder {

		public Object Event = new Object();
	    public BaseMsg Message = null;

	}
}
