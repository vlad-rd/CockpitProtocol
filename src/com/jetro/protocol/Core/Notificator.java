package com.jetro.protocol.Core;

import java.util.Vector;

import android.app.Activity;


public class Notificator {
	 
    // Our collection of classes that are subscribed as listeners of our
    protected Vector<IMessageSubscriber> _listeners = new Vector<IMessageSubscriber>();
     
    // Method for listener classes to register themselves
    public void AddListener(IMessageSubscriber listener)
    {
        synchronized(_listeners)
        {
        	_listeners.addElement(listener);
        }
    }
    
    public void RemoveListener(IMessageSubscriber listener)
    {
        synchronized(_listeners)
        {
        	IMessageSubscriber toRemove = null;
        	for(IMessageSubscriber l  : _listeners)
        	{
        		if(l == listener){
        			toRemove = l; break;
        		}
        	}
        	if(toRemove != null) _listeners.remove(toRemove);
        }        	
    }
    
    protected void fireEvent(final BaseMsg msg)
    {
        if (_listeners != null && !_listeners.isEmpty())
        {
        	synchronized(_listeners)
            {
        		for(final IMessageSubscriber el : _listeners)
        		{
        			try
        			{
        				// TODO: this have to run in the UI thread
        				if (el instanceof Activity) {
        					Activity activity = (Activity) el;
        					activity.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									el.ProcessMsg(msg);
								}
							});
        				} else {
        					new asyncRunner(msg,el).Run();
        				}
        			}
        			catch(Exception ex)
        			{
        				System.out.println(ex.toString());
        			}
        		}
            }
        }
    }
    
    protected void fireConnectionBroken()
    {
        if (_listeners != null && !_listeners.isEmpty())
        {
        	synchronized(_listeners)
            {
        		for(IMessageSubscriber el : _listeners)
        		{
        			try
        			{
        				new asyncBrokenConnectionNotificator(el).Run();
        			}
        			catch(Exception ex)
        			{
        				System.out.println(ex.toString());
        			}
        		}
            }
        }
    }
    
    private class asyncRunner
    {
    	private BaseMsg message = null;
    	IMessageSubscriber subscriber = null;
    	
    	public asyncRunner(BaseMsg msg, IMessageSubscriber sr){
    		message = msg;
    		subscriber = sr;
    	}
    	public void Run(){
    		Thread thread = new Thread(){
			    public void run(){
			    	try {
			    		subscriber.ProcessMsg(message);
			    	}
			    	catch(Exception ex){
			    		System.out.println(ex.toString());
			    	}
			    }
			 };
			 thread.start();
    	}
    }
    
    private class asyncBrokenConnectionNotificator
    {
    	private IMessageSubscriber subscriber = null;
    	
    	public asyncBrokenConnectionNotificator(IMessageSubscriber sr){
     		subscriber = sr;
    	}
    	public void Run(){
    		Thread thread = new Thread(){
			    public void run(){
			    	try {
			    		subscriber.ConnectionIsBroken();
			    	}
			    	catch(Exception ex){
			    		System.out.println(ex.toString());
			    	}
			    }
			 };
			 thread.start();
    	}
    }
}
