package com.jetro.protocol.Core.Net;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.util.ByteArrayBuffer;

import com.jetro.protocol.Core.BaseMsg;
import com.jetro.protocol.Core.Net.ClientChannel.NaiveTrustManager;
import com.jetro.protocol.Protocols.TsSession.StartRdpMsg;


import android.util.Log;

public class ChannelRdpTunnel {
	
	StartRdpMsg _StartRdpMsg = null;
	
	String TAG = "ChannelRdpTunnel";
	
	String _ConnectorAddress = "";
	
	int _ConnectorPort = 0;
	
	public ChannelRdpTunnel()
	{
	
	}
	
	public ChannelRdpTunnel(StartRdpMsg msg, String connectorAddress, int connectorPort) {
		_StartRdpMsg = msg;
		_ConnectorAddress = connectorAddress;
		_ConnectorPort = connectorPort;
	}
	
	ServerSocket _ServerSocket = null;
	
	public int Port = 0;
	
	public Boolean Start(int timeout, final String remoteAddress,final int remotePort,final StartRdpMsg msg)
	{
		try
		{
			_ServerSocket = new ServerSocket();
		}
		catch(Exception ex)
		{
			return false;
		}
		
		Thread thread = new Thread(){
		    public void run(){
		    	try {
		    		synchronized (_ServerSocket) {
		    			_ServerSocket.bind(null);
			    		Port =  _ServerSocket.getLocalPort();
						_ServerSocket.notify();
					}
					
					Log.i(TAG,"Port = " + Port);
					
					Socket left = null;
					
					Socket right = null;
							
					for(;;)
					{
						left = _ServerSocket.accept();
						Log.i(TAG,"accepted local RDP connection");
						left.setTcpNoDelay(true);
						//left.setSoTimeout(100);
						right = new Socket(remoteAddress, remotePort);
						Log.i(TAG,"Connected to remote " + remoteAddress + ":" + remotePort);
						right.getOutputStream().write(msg.pack());
						Log.i(TAG,"Sent first message to remote " + remoteAddress + ":" + remotePort);
						BaseMsg firstMsg =  getFirstMessage(right);
						if(firstMsg == null){
							left.close();
							right.close();
						}
						
						right.setTcpNoDelay(true);
						//right.setSoTimeout(100);
					
						Log.i(TAG,"Starting tunneling");
						//makeTunnelingSwapping(left, right);
						CommunicationThread lth = new CommunicationThread("Left communicator",left,right);
						new Thread(lth).start();
						CommunicationThread rth = new CommunicationThread("Right communicator",right, left);
						new Thread(rth).start();
					}
					
					
				} catch (IOException e) {
						
					e.printStackTrace();
				}
		    }
		 };
		 
		 thread.start();
		 
		 try {
				synchronized (_ServerSocket) {
					_ServerSocket.wait(timeout);
				}
			} catch (InterruptedException e) {
				Log.e(TAG, "ERROR: ", e);
				return false;
			}
		return _ServerSocket.isBound();
	}
	
	public Boolean StartSSL(int timeout, final String remoteAddress,final int remotePort,final StartRdpMsg msg)
	{
		try
		{
			_ServerSocket = new ServerSocket();
		}
		catch(Exception ex)
		{
			return false;
		}
		
		Thread thread = new Thread(){
		    public void run(){
		    	try {
		    		synchronized (_ServerSocket) {
		    			_ServerSocket.bind(null);
			    		Port =  _ServerSocket.getLocalPort();
						_ServerSocket.notify();
					}
					
					Log.i(TAG,"Port = " + Port);
					
					Socket left = null;
					
					SSLSocket right = null;
							
					for(;;)
					{
						left = _ServerSocket.accept();
						Log.i(TAG,"accepted local RDP connection");
						left.setTcpNoDelay(true);
						//left.setSoTimeout(100);
						SSLSocketFactory factory = getSocketFactory();
						right = (SSLSocket) factory.createSocket(remoteAddress,
								remotePort);
						right.startHandshake();
						
						Log.i(TAG,"Connected to remote " + remoteAddress + ":" + remotePort);
						right.getOutputStream().write(msg.pack());
						Log.i(TAG,"Sent first message to remote " + remoteAddress + ":" + remotePort);
						BaseMsg firstMsg =  getFirstMessage(right);
						if(firstMsg == null){
							left.close();
							right.close();
						}
						
						right.setTcpNoDelay(true);
						//right.setSoTimeout(100);
					
						Log.i(TAG,"Starting SSL tunneling");
						//makeTunnelingSwapping(left, right);
						CommunicationThread lth = new CommunicationThread("Left communicator",left,right);
						new Thread(lth).start();
						CommunicationThread rth = new CommunicationThread("Right communicator", right, left);
						new Thread(rth).start();
					}
					
					
				} catch (IOException e) {
						
					e.printStackTrace();
				}
		    }
		 };
		 
		 thread.start();
		 
		 try {
				synchronized (_ServerSocket) {
					_ServerSocket.wait(timeout);
				}
			} catch (InterruptedException e) {
				Log.e(TAG, "ERROR: ", e);
				return false;
			}
		return _ServerSocket.isBound();
	}
	
	private SSLSocketFactory getSocketFactory() {
		SSLSocketFactory sslSocketFactory = null;
		try {
			TrustManager[] tm = new TrustManager[] { new NaiveTrustManager() };
			SSLContext context = SSLContext.getInstance("TLSv1");
			context.init(new KeyManager[0], tm, new SecureRandom());

			sslSocketFactory = (SSLSocketFactory) context.getSocketFactory();

		} catch (KeyManagementException e) {
			Log.e(TAG, "ERROR: ", e);
			Log.e("No SSL algorithm support: " + e.getMessage(), e.toString());
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "ERROR: ", e);
			Log.e("Exception when setting up the Naive key management.",
					e.toString());
		}

		return sslSocketFactory;
	}
			
	private BaseMsg getFirstMessage(Socket right)
	{
		try
		{
			byte[] buffer = new byte[4096];
			int readen = right.getInputStream().read(buffer);
			Log.i(TAG, "getFirstMessage received " + readen);
			BaseMsg firstResp = BaseMsg.createMessage(buffer);
			return firstResp;
		}
		catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
	}
	
	public void Stop(){
		try{
			if(_ServerSocket != null) _ServerSocket.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	class NaiveTrustManager implements X509TrustManager {
		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {

		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {

		}
	}
	
}