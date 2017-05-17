package fi.oulu.tol.esde019.cwpclient019.model;

import android.os.Message;

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
    }

    @Override
    public void lineDown() throws IOException {
        protocolImplementation.lineDown();
    }

    @Override
    public void connect(String serverAddr, int serverPort, int frequency) throws IOException {
        protocolImplementation.connect(serverAddr, serverPort, frequency);
    }

    public void disconnect() throws IOException {
        protocolImplementation.disconnect();
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    public void setFrequency(int frequency) throws IOException {
        protocolImplementation.setFrequency(frequency);
    }

    public int frequency() {
        return protocolImplementation.frequency();
    }

    @Override
    public boolean lineIsUp() {
        return protocolImplementation.lineIsUp();
    }

    @Override
    public void onEvent(CWProtocolListener.CWPEvent event, int param) {
        setChanged();
        notifyObservers(event);
    }
}