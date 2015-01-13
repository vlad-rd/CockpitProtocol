package com.jetro.protocol.Core.Net;

import java.io.IOException;
import java.net.Socket;

import android.util.Log;
public class CommunicationThread implements Runnable {
		        private Socket left;
		        private Socket right;
		        String _tag = "";
		        public CommunicationThread(String tag,Socket ls, Socket rs) {
		        	_tag = tag;
		        	left = ls;
		        	right = rs;
		        }
		        public void run() {
		        	try {
		        		int il = 0;
		        		byte[] bufferLeft = new byte[10240];
		        		while(true){
		        			il = 0;
		        			try {
		        					il = left.getInputStream().read(bufferLeft);
		        			    } 
		        				catch (java.net.SocketTimeoutException e) {
		        			        continue;
		        			    }
		        				catch(Exception e)
		        				{
		        					right.close();
		        					left.close();
		        					Log.i(_tag,"Client close");
		        					break;
		        				}
		        				
		        				if(il == -1)
		        				{
		        					right.close();
		        					left.close();
		        					Log.i(_tag,"Client close " + il);
		        					break;
		        				}
		        				Log.i(_tag,"left.read = " + il);
		        				
		        											
		        				try{
		        					right.getOutputStream().write(bufferLeft, 0, il);
		        					right.getOutputStream().flush();
		        				}
		        				catch(Exception e)
		        				{
		        					right.close();
		        					left.close();
		        					Log.i(_tag,"write right close");
		        					break;
		        				}
		        			}
		        		} catch (IOException e) {
		        			try {
		        				right.close();
		        			} catch (IOException e1) {
		        				// TODO Auto-generated catch block
		        				e1.printStackTrace();
		        			}
		        			try {
		        				left.close();
		        			} catch (IOException e1) {
		        				// TODO Auto-generated catch block
		        				e1.printStackTrace();
		        			}
		        			e.printStackTrace();
		        		}
		        }
}