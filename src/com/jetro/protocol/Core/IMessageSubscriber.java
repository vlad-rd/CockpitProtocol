package com.jetro.protocol.Core;

public interface IMessageSubscriber {
	void ProcessMsg(BaseMsg msg);
	void ConnectionIsBroken();
}
