package fi.oulu.tol.esde019.cwpclient019.model;

import java.io.IOException;
import java.util.Observable;

import fi.oulu.tol.esde019.cwpclient019.cwprotocol.CWPControl;
import fi.oulu.tol.esde019.cwpclient019.cwprotocol.CWPMessaging;

public class CWPModel extends Observable implements CWPMessaging, CWPControl {

    public enum CWPState { Disconnected, Connected, LineUp, LineDown };
    private CWPState currentState = CWPState.Disconnected;
    public static final int DEFAULT_FREQUENCY = -1;

    @Override
    public void lineUp() throws IOException {
        currentState = CWPState.LineUp;
        setChanged();
        notifyObservers(currentState);
    }

    @Override
    public void lineDown() throws IOException {
        currentState = CWPState.LineDown;
        setChanged();
        notifyObservers(currentState);
    }

    @Override
    public void connect(String serverAddr, int serverPort, int frequency) throws IOException {
        currentState = CWPState.Connected;
        setChanged();
        notifyObservers(currentState);
    }

    public void disconnect() throws IOException {
        currentState = CWPState.Disconnected;
        setChanged();
        notifyObservers(currentState);
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
}