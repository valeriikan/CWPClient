package fi.oulu.tol.esde019.cwpclient019.cwprotocol;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

public class CWProtocolImplementation implements CWPControl, CWPMessaging, Runnable {

    public CWProtocolImplementation(CWProtocolListener listener_p) {
        listener = listener_p;
    }

    public enum CWPState { Disconnected, Connected, LineUp, LineDown };
    private CWPState currentState = CWPState.Disconnected;
    private CWPState nextState = currentState;
    private int currentFrequency = DEFAULT_FREQUENCY;
    private CWPConnectionReader connection = null;
    private Handler receiveHandler = new Handler();
    private int messageValue;
    private CWProtocolListener listener;

    private final static String TAG = "CWP019";

    @Override
    public void addObserver(Observer observer) {

    }

    @Override
    public void deleteObserver(Observer observer) {

    }

    @Override
    public void connect(String serverAddr, int serverPort, int frequency) throws IOException {
        connection = new CWPConnectionReader(this);
        connection.startReading();
    }

    @Override
    public void disconnect() throws IOException {
        try {
            connection.stopReading();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        connection = null;
    }

    @Override
    public boolean isConnected() {
        if (currentState == CWPState.Connected) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void setFrequency(int frequency) throws IOException {

    }

    @Override
    public int frequency() {
        return 0;
    }

    @Override
    public void lineUp() throws IOException {
        currentState = CWPState.LineUp;
    }

    @Override
    public void lineDown() throws IOException {
        currentState = CWPState.LineDown;
    }

    @Override
    public boolean lineIsUp() {
        if (currentState == CWPState.LineUp) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void run() {
        switch (nextState) {
            case Connected:
                Log.d(TAG, "State change to connected happening...");
                currentState = nextState;
                listener.onEvent(CWProtocolListener.CWPEvent.EConnected, 0);
                break;
            case Disconnected:
                Log.d(TAG, "State change to disconnected happening...");
                currentState = nextState;
                listener.onEvent(CWProtocolListener.CWPEvent.EDisconnected, 0);
                break;
            case LineUp:
                Log.d(TAG, "State change to LineUp happening...");
                currentState = nextState;
                listener.onEvent(CWProtocolListener.CWPEvent.ELineUp, 0);
                break;
            case LineDown:
                Log.d(TAG, "State change to LineDown happening...");
                currentState = nextState;
                listener.onEvent(CWProtocolListener.CWPEvent.ELineDown, 0);
                break;
        }
    }

    private class CWPConnectionReader extends Thread {

        private boolean running = false;
        private Runnable myProcessor = null;
        private static final String TAG = "CWPReader";

        // Used before networking for timing cw signals
        private Timer myTimer = null;
        private TimerTask myTimerTask = null;

        CWPConnectionReader(Runnable processor) {
            myProcessor = processor;
        }

        void startReading() {
            running = true;
            start();
        }

        void stopReading() throws InterruptedException {
            myTimer.cancel();
            running = false;
            myTimer = null;
            currentState = CWPState.Disconnected;
        }

        private void doInitialize() throws InterruptedException {
            currentState = CWPState.Connected;
            myTimer = new Timer();
            myTimerTask = new TimerTask() {
                @Override
                public void run() {
                    if (currentState == CWPState.LineDown) {
                        try {
                            changeProtocolState(CWPState.LineUp, 0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (currentState == CWPState.LineUp) {
                        try {
                            changeProtocolState(CWPState.LineDown, 0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            myTimer.scheduleAtFixedRate(myTimerTask,0,2000);
        }

        @Override
        public void run() {
            try {
                doInitialize();
                currentState = CWPState.LineDown;
                while (running) {

                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void changeProtocolState(CWPState state, int param) throws InterruptedException {
            Log.wtf(TAG, "Change protocol state to " + state);
            nextState = state;
            messageValue = param;
            receiveHandler.post(myProcessor); // <<<< tell the android.os.Handler to post an event to myProcessor.
        }
    }
}