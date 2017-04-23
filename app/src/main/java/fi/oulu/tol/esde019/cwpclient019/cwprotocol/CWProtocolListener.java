package fi.oulu.tol.esde019.cwpclient019.cwprotocol;

public interface CWProtocolListener {
    public enum CWPEvent {EConnected, EChangedFrequency, ELineUp, ELineDown, EDisconnected};
    public void onEvent(CWPEvent event, int param);
}
