package com.jetro.protocol.Core;

public enum ClassID
{
    None((short)0),
    KeyExchangeMsg((short)1),

    //Controller >= 5100  < 5200
    CockpitSiteInfoMsg((short)5100),
    LoginScreenImageMsg((short)5101),
    LoginMsg((short)5102),
    ResetPasswordMsg((short)5103),
    MyApplicationsMsg((short)5104),
    GetTsMsg((short)5105),
    LogoutMsg((short)5106),
    TicketValidationMsg((short)5107),
    ApplicationIconMsg((short)5108),
    //TS Session >= 5200  < 5300
    ShowWindowMsg((short)5201),
    StartApplicationMsg((short)5202),
    ShowTaskListMsg((short)5203),
    WindowDestroyedMsg((short)5204),
    ShowKeyBoardMsg((short)5205),
    WindowCreatedMsg((short)5206),
    KillProcessMsg((short)5207),
    SessionReadyMsg((short)5208),
    StartRdpMsg((short)5209),
    SessionAvailableApplicationsMsg((short)5210), //between session and TS service
    SessionEndMsg((short)5211),
    WindowCloseMsg((short)5212),
    WindowChangedMsg((short)5213),

    TunnelData((short)9001),

    Error((short)9999);
    
    private short value = 0;

    private ClassID(short classID) {
            this.value = classID;
    }
    
    public short ValueOf(){
    	return value;
    }
    
    public static ClassID GetID(short id){

    	for(ClassID cid: ClassID.values()){
            if(cid.ValueOf() == id) return cid;
    	}
    	return None;
    }
}
