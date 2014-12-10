package com.jetro.protocol.Core.Net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.SocketChannel;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.util.Log;

import com.jetro.protocol.Core.BaseMsg;
import com.jetro.protocol.Core.IConnectionCreationSubscriber;
import com.jetro.protocol.Core.Notificator;
import com.jetro.protocol.Protocols.Generic.ErrorMsg;

public class ClientChannel extends Notificator {

	private static final String TAG = ClientChannel.class.getSimpleName();

	public static final int TIME_OUT = 2000;

	private static ClientChannel _Instance = null;

	public static ClientChannel getInstance() {
		return _Instance;
	}

	public Map<String, MessageHolder> _WaitingRequests = new HashMap<String, MessageHolder>();

	private SocketChannel _Socket;

	private SSLSocket _SSLSocket;

	private boolean _SSL = false;

	ByteArrayOutputStream _ByteArrayOutputStream = new ByteArrayOutputStream(
			40960);

	ByteBuffer _Chunk = ByteBuffer.allocate(4096);

	private ClientChannel() {
	}

	public BaseMsg SendReceive(BaseMsg msg, int timeout) {
		try {
			MessageHolder mh = new MessageHolder();
			synchronized (_WaitingRequests) {
				_WaitingRequests.put(msg.MsgId, mh);
			}
			try {
				synchronized (_Socket) {
					_Socket.socket().getOutputStream().write(msg.pack());
				}
			} catch (IOException e) {
				Log.e(TAG, "ERROR: ", e);
				Stop();
				return new ErrorMsg(msg.MsgId, 999, "Unexpected error");
			}

			synchronized (mh.Event) {
				mh.Event.wait(timeout);
			}

			BaseMsg resp = null;

			synchronized (_WaitingRequests) {
				if (_WaitingRequests.containsKey(msg.MsgId)) {
					resp = _WaitingRequests.get(msg.MsgId).Message;
					Log.i("ClientChannel", resp.toString());
				}
			}

			return resp;

		} catch (InterruptedException e) {
			Log.e(TAG, "ERROR: ", e);
			return new ErrorMsg(msg.MsgId, 5, "Timeout error");
		} finally {
			try {
				synchronized (_WaitingRequests) {
					if (_WaitingRequests.containsKey(msg.MsgId))
						_WaitingRequests.remove(msg.MsgId);
				}
			} catch (Exception e) {
				Log.e(TAG, "ERROR: ", e);
			}
		}
	}

	public BaseMsg SendReceiveInUI(BaseMsg msg, int timeout) {
		try {
			MessageHolder mh = new MessageHolder();
			synchronized (_WaitingRequests) {
				_WaitingRequests.put(msg.MsgId, mh);
			}

			msgAsyncSender sender = new msgAsyncSender(msg);

			sender.Send();

			synchronized (mh.Event) {
				mh.Event.wait(timeout);
			}

			BaseMsg resp = null;

			synchronized (_WaitingRequests) {
				if (_WaitingRequests.containsKey(msg.MsgId)) {
					resp = _WaitingRequests.get(msg.MsgId).Message;
					Log.i("ClientChannel", resp.toString());
				}
			}

			return resp;

		} catch (InterruptedException e) {
			Log.e(TAG, "ERROR: ", e);
			return new ErrorMsg(msg.MsgId, 5, "Timeout error");
		} finally {
			try {
				synchronized (_WaitingRequests) {
					if (_WaitingRequests.containsKey(msg.MsgId))
						_WaitingRequests.remove(msg.MsgId);
				}
			} catch (Exception e) {
				Log.e(TAG, "ERROR: ", e);
			}
		}
	}

	public void SendReceiveAsync(BaseMsg msg) {
		msgAsyncSender sender = new msgAsyncSender(msg);
		sender.Send();
	}

	public void threadProcSSL() {
		Thread.currentThread().setName("ClientChannel-ListenerSSL");
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		int readen = 0;
		while (true) {
			try {
				if (_SSLSocket == null || !_SSLSocket.isConnected()) {
					fireConnectionBroken();
					return;
				}
				_Chunk.position(0);
				readen = _SSLSocket.getInputStream().read(_Chunk.array());
				if (readen > 0)
					streamProc(readen);
			} 
			catch(SocketException se)
			{
				Log.e(TAG, "Socket close");
				fireConnectionBroken();
				try {
					if (_SSLSocket != null)
						_SSLSocket.close();
				} catch (Exception ex) {
					//Log.e(TAG, "ERROR: ", ex);
				}
				break;
			}
			catch (IOException e) {
				Log.e(TAG, "ERROR: ", e);
				fireConnectionBroken();
				try {
					if (_SSLSocket != null)
						_SSLSocket.close();
				} catch (Exception ex) {
					Log.e(TAG, "ERROR: ", ex);
				}
				_SSLSocket = null;
				break;
			}
		}
		Log.i("ClientChannel", "threadProcSSL exit");
	}

	void streamProc(int readen) throws IOException {
		Log.i("ClientChannel", "received " + readen);
		_ByteArrayOutputStream.write(_Chunk.array(), 0, readen);
		for (;;) {
			byte[] original = _ByteArrayOutputStream.toByteArray();

			BaseMsg msg = BaseMsg.createMessage(original);

			if (msg == null)
				break;

			if (msg.header.MessageLength == original.length) {
				_ByteArrayOutputStream.reset();
			} else {
				byte[] rest = new byte[original.length
						- msg.header.MessageLength];
				System.arraycopy(original, msg.header.MessageLength, rest, 0,
						rest.length);
				_ByteArrayOutputStream.reset();
				_ByteArrayOutputStream.write(rest);
			}

			if (_WaitingRequests.containsKey(msg.MsgId)) {
				_WaitingRequests.get(msg.MsgId).Message = msg;
				synchronized (_WaitingRequests.get(msg.MsgId).Event) {
					_WaitingRequests.get(msg.MsgId).Event.notify();
				}
			} else
				fireEvent(msg);
		}
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
		int readen = 0;

		while (true) {
			try {
				if (!_Socket.isConnected()) {
					fireConnectionBroken();
					return;
				}
				_Chunk.position(0);
				readen = _Socket.read(_Chunk);
				if (readen > 0)
					streamProc(readen);
			} 
			catch(AsynchronousCloseException ce)
			{
				Log.e(TAG, "Channel close");
				fireConnectionBroken();
				break;
			}
			catch (IOException e) {
				Log.e(TAG, "ERROR: ", e);
				fireConnectionBroken();
				try {
					if (_Socket != null)
						_Socket.close();
				} catch (Exception e2) {
					Log.e(TAG, "ERROR: ", e2);
				}
				_Socket = null;
				break;
			}
		}
		Log.i("ClientChannel", "threadProc exit");
	}

	private String _Address = "";
	private int _Port = 0;

	public static boolean Create(String address, int port, int timeout, IConnectionCreationSubscriber cs) {
		if (_Instance != null) {
			_Instance.Stop();
		}
		_Instance = new ClientChannel();

		return _Instance.create(address, port, timeout, cs);
	}

	public static boolean CreateSSL(String address, int port, int timeout, IConnectionCreationSubscriber cs) {
		if (_Instance != null) {
			_Instance.Stop();
		}
		_Instance = new ClientChannel();

		return _Instance.createSSL(address, port, timeout, cs);
	}
	
	private void connectionNotify(final IConnectionCreationSubscriber cs,final boolean result,final String text){
		Thread thread = new Thread() {
			public void run() {
				try {
					if(cs != null) cs.ConnectionCreated(result, text);
				} catch (Exception e) {
					Log.e(TAG, "ERROR: ", e);					
				}
			}
		};
		thread.start();
	}

	private boolean create(String address, int port, int timeout, final IConnectionCreationSubscriber cs) {
		_Address = address;
		_Port = port;

		try {
			_Socket = SocketChannel.open();
		} catch (IOException e) {
			Log.e(TAG, "ERROR: ", e);
			_Socket = null;
			connectionNotify(cs, false, e.getMessage());
			return false;
		}
		
		Thread thread = new Thread() {
			public void run() {
				Log.i("Connector", "Starts");
				try {
					_Socket.connect(new InetSocketAddress(_Address, _Port));
					synchronized (_Socket) {
						_Socket.notify();
					}
					connectionNotify(cs, true, "");
				} catch (IOException e) {
					Log.e(TAG, "ERROR: ", e);
					synchronized (_Socket) {
						_Socket.notify();
					}
					connectionNotify(cs, false, e.getMessage());
				}
			}
		};
		thread.start();

		Log.i("Connector", "End");

		try {
			synchronized (_Socket) {
				_Socket.wait(timeout);
			}
		} catch (InterruptedException e) {
			Log.e(TAG, "ERROR: ", e);
			return false;
		}
		if (_Socket.isConnected()) {
			Thread threadListener = new Thread() {
				public void run() {
					threadProc();
				}
			};
			threadListener.start();
			return true;
		} else
			return false;
	}

	public boolean Create(String address, int port) {
		try {
			_Socket = SocketChannel.open();
			_Socket.connect(new InetSocketAddress(address, port));
			Thread thread = new Thread() {
				public void run() {
					threadProc();
				}
			};
			thread.start();
			return true;
		} catch (IOException e) {
			Log.e(TAG, "ERROR: ", e);
			return false;
		}
	}

	private boolean createSSL(String address, int port, int timeout, final IConnectionCreationSubscriber cssl) {
		_Address = address;
		_Port = port;
		_SSL = true;
		final Object sync = new Object();

		Thread thread = new Thread() {
			public void run() {
				Log.i("Connector SSL", "Starts");
				try {

					SSLSocketFactory factory = getSocketFactory();
					_SSLSocket = (SSLSocket) factory.createSocket(_Address,
							_Port);
					_SSLSocket.startHandshake();

					synchronized (sync) {
						sync.notify();
					}
					connectionNotify(cssl, true, "");
				} catch (IOException e) {
					Log.e(TAG, "ERROR: ", e);
					synchronized (sync) {
						sync.notify();
					}
					connectionNotify(cssl,false, e.getMessage());
				}

			}
		};
		thread.start();

		Log.i("Connector", "End");

		try {
			synchronized (sync) {
				sync.wait(timeout);
			}
		} catch (InterruptedException e) {
			Log.e(TAG, "ERROR: ", e);
			return false;
		}
		if (_SSLSocket != null && _SSLSocket.isConnected()) {
			Thread threadListener = new Thread() {
				public void run() {
					threadProcSSL();
				}
			};
			threadListener.start();
			return true;
		} else
			return false;
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

	public void Stop() {
		_Instance = null;
		Thread thread = new Thread() {
			public void run() {
				try {
					if (_Socket != null) {
						_Socket.close();
						_Socket = null;
					}
					if (_SSLSocket != null) {
						_SSLSocket.close();
						_SSLSocket = null;
					}
					if (_ByteArrayOutputStream != null) {
						_ByteArrayOutputStream.close();
					}
					
				} catch (IOException e) {
					Log.e(TAG, "ERROR: ", e);
				}
			}
		};
		thread.start();
	}

	public boolean Send(BaseMsg msg) {
		try {
			_Socket.socket().getOutputStream().write(msg.pack());
			return true;
		} catch (Exception e) {
			Log.e(TAG, "ERROR: ", e);
			return false;
		}
	}

	public void SendAsyncTimeout(BaseMsg msg, int timeout) {
		try {
			msgSenderCallBack m = new msgSenderCallBack(msg, timeout);
			m.Send();
		} catch (Exception e) {
			Log.e(TAG, "ERROR: ", e);
		}
	}

	public void SendAsync(BaseMsg msg) {
		try {
			msgAsyncSender m = new msgAsyncSender(msg);
			m.Send();
		} catch (Exception e) {
			Log.e(TAG, "ERROR: ", e);
		}
	}
	
	private class msgAsyncSender {
		BaseMsg msg;

		public msgAsyncSender(BaseMsg m) {
			msg = m;
		}

		public void Send() {
			Thread thread = new Thread() {
				public void run() {
					try {
						if (!_SSL)
							_Socket.socket().getOutputStream()
									.write(msg.pack());
						else {
							_SSLSocket.getOutputStream().write(msg.pack());
							_SSLSocket.getOutputStream().flush();
						}
					} catch (IOException e) {
						Log.e(TAG, "ERROR: ", e);
						try {
							_Socket.close();
						} catch (IOException e1) {
							Log.e(TAG, "ERROR: ", e1);
						} finally {
							_Socket = null;
						}
					}
				}
			};
			thread.start();
		}
	}

	private class msgSenderCallBack {
		BaseMsg msg;
		int timeout;

		public msgSenderCallBack(BaseMsg m, int tout) {
			msg = m;
			timeout = tout;
		}

		public void Send() {
			Thread thread = new Thread() {
				public void run() {
					try {
						MessageHolder mh = new MessageHolder();
						synchronized (_WaitingRequests) {
							_WaitingRequests.put(msg.MsgId, mh);
						}

						if (!_SSL) {
							synchronized (_Socket) {
								_Socket.socket().getOutputStream()
										.write(msg.pack());
							}
						} else {
							synchronized (_SSLSocket) {
								_SSLSocket.getOutputStream().write(msg.pack());
								_SSLSocket.getOutputStream().flush();
							}
						}

						BaseMsg resp = null;

						synchronized (mh.Event) {
							try {
								mh.Event.wait(timeout);
							} catch (InterruptedException e) {
								Log.e(TAG, "ERROR: ", e);
								resp = new ErrorMsg(msg.MsgId, 5, "Timeout occured 1");
								fireEvent(resp);
								return;
							}
						}
						synchronized (_WaitingRequests) {
							if (_WaitingRequests.containsKey(msg.MsgId)) {
								resp = _WaitingRequests.get(msg.MsgId).Message;
								if (resp == null)
									resp = new ErrorMsg(msg.MsgId, 5,
											"Timeout occured for msg id = "
													+ msg.MsgId);
								Log.i("ClientChannel", resp.toString());
							}
						}

						try {
							fireEvent(resp);
						} catch (Exception e) {
							Log.e(TAG, "ERROR: ", e);
						}

					} catch (IOException e) {
						Log.e(TAG, "ERROR: ", e);
						try {
							if (!_SSL)
								_Socket.close();
							else
								_SSLSocket.close();
						} catch (IOException e1) {
							Log.e(TAG, "ERROR: ", e1);
						} finally {
							_Socket = null;
							_SSLSocket = null;
						}
						e.printStackTrace();
					} finally {
						try {
							synchronized (_WaitingRequests) {
								if (_WaitingRequests.containsKey(msg.MsgId))
									_WaitingRequests.remove(msg.MsgId);
							}
						} catch (Exception e) {
							Log.e(TAG, "ERROR: ", e);
						}
					}
				}
			};
			thread.start();
		}

		public final SSLSocketFactory getSocketFactory() {
			SSLSocketFactory sslSocketFactory = null;
			try {
				TrustManager[] tm = new TrustManager[] { new NaiveTrustManager() };
				SSLContext context = SSLContext.getInstance("TLSv1");
				context.init(new KeyManager[0], tm, new SecureRandom());

				sslSocketFactory = (SSLSocketFactory) context
						.getSocketFactory();

			} catch (KeyManagementException e) {
				Log.e(TAG, "ERROR: ", e);
				Log.e("No SSL algorithm support: " + e.getMessage(),
						e.toString());
			} catch (NoSuchAlgorithmException e) {
				Log.e(TAG, "ERROR: ", e);
				Log.e("Exception when setting up the Naive key management.",
						e.toString());
			}

			return sslSocketFactory;
		}
	}

	private class MessageHolder {

		public Object Event = new Object();
		public BaseMsg Message = null;

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
