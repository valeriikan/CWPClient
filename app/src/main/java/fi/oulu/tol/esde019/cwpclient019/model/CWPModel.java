package fi.oulu.tol.esde019.cwpclient019.model;

import java.io.IOException;
import java.util.Observable;

import fi.oulu.tol.esde019.cwpclient019.cwprotocol.CWPControl;
import fi.oulu.tol.esde019.cwpclient019.cwprotocol.CWPMessaging;
import fi.oulu.tol.esde019.cwpclient019.cwprotocol.CWProtocolImplementation;
import fi.oulu.tol.esde019.cwpclient019.cwprotocol.CWProtocolListener;

public class CWPModel extends Observable implements CWPMessaging, CWPControl, CWProtocolListener {

    private CWProtocolImplementation protocolImplementation = new CWProtocolImplementation(this);

    @Override
    public void lineUp() throws IOException {
        protocolImplementation.lineUp();
        /*currentState = CWPState.LineUp;
        setChanged();
        notifyObservers(currentState);*/
    }

    @Override
    public void lineDown() throws IOException {
        protocolImplementation.lineDown();
        /*currentState = CWPState.LineDown;
        setChanged();
        notifyObservers(currentState);*/
    }

    @Override
    public void connect(String serverAddr, int serverPort, int frequency) throws IOException {
        protocolImplementation.connect("serverAddr", 2, 2);
        /*currentState = CWPState.Connected;
        setChanged();
        notifyObservers(currentState);*/
    }

    public void disconnect() throws IOException {
        protocolImplementation.disconnect();
        /*currentState = CWPState.Disconnected;
        setChanged();
        notifyObservers(currentState);*/
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    public void setFrequency(int frequency) throws IOException {

    }

    public int frequency() {
        return frequency();
    }

    @Override
    public boolean lineIsUp() {
        return false;
    }

    @Override
    public void onEvent(CWProtocolListener.CWPEvent event, int param) {
        setChanged();
        notifyObservers(event);
    }
}