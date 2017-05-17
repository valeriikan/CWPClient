package fi.oulu.tol.esde019.cwpclient019.cwprotocol;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Observer;
import java.util.concurrent.Semaphore;

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

    private static final int BUFFER_LENGTH = 64;
    private static final int FORBIDDEN_VALUE = -2147483648;
    private OutputStream nos = null; //Network Output Stream
    private ByteBuffer outBuffer = null;
    private String serverAddr = null;
    private int serverPort = -1;
    private boolean lineUpByUser = false;
    private boolean lineUpByServer = false;
    private int lineUpTimeStamp;
    private long sessionInitTime;


    private Semaphore lock = new Semaphore(1);

    private final static String TAG = "CWP019";

    @Override
    public void addObserver(Observer observer) {

    }

    @Override
    public void deleteObserver(Observer observer) {

    }

    @Override
    public void connect(String serverAddr, int serverPort, int frequency) throws IOException {
        this.serverPort = serverPort;
        this.serverAddr = serverAddr;
        setFrequency(frequency);
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
        serverAddr = null;
        serverPort = 0;
        currentFrequency = DEFAULT_FREQUENCY;
        Log.wtf(TAG, "Disconnect finished");
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
        if (currentFrequency != frequency) {
            Log.wtf(TAG, "Start frequency change");
            if (frequency > 0) {
                currentFrequency = -frequency;
            } else if (frequency == 0) {
                currentFrequency = DEFAULT_FREQUENCY;
            } else {
                currentFrequency = frequency;
            }
            sendFrequency();
        }

    }

    @Override
    public int frequency() {
        return currentFrequency;
    }

    private void sendFrequency() throws IOException {
        boolean didIt = false;
        try {
            Log.wtf(TAG, "Trying to send frequency");
            lock.acquire();
            if (currentState == CWPState.LineDown) {
                try {
                    Log.wtf(TAG, "Sending frequency");
                    sendMessage(currentFrequency);
                    currentState = CWPState.Connected;
                    didIt = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.wtf(TAG, "State is not line down, set frequency later");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.release();
        }
        if (didIt) {
            listener.onEvent(CWProtocolListener.CWPEvent.EConnected, 0);
        }
    }

    @Override
    public void lineUp() throws IOException {
        boolean didIt = false;
        try {
            lock.acquire();
            if (!lineUpByUser && (currentState == CWPState.LineDown || currentState == CWPState.LineUp)) {
                Log.wtf(TAG, "User sets line up");
                lineUpTimeStamp = (int)(System.currentTimeMillis()-sessionInitTime);
                sendMessage(lineUpTimeStamp);
                if (currentState == CWPState.LineDown && !lineUpByServer) {
                    currentState = CWPState.LineUp;
                    didIt = true;
                }
                lineUpByUser = true;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.release();
        }
        if (didIt) {
            listener.onEvent(CWProtocolListener.CWPEvent.ELineUp, lineUpTimeStamp);
        }
    }

    @Override
    public void lineDown() throws IOException {
        boolean didIt = false;
        short msg = 0;
        try {
            lock.acquire();
            if (lineUpByUser && (currentState == CWPState.LineUp)) {
                Log.wtf(TAG, "User sets line down");
                msg = (short)(System.currentTimeMillis()-sessionInitTime-lineUpTimeStamp);
                sendMessage(msg);
                lineUpByUser = false;
                if (!lineUpByServer) {
                    currentState = CWPState.LineDown;
                    didIt = true;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.release();
        }
        if (didIt) {
            listener.onEvent(CWProtocolListener.CWPEvent.ELineDown, msg);
        }
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
        int newMessageValue = messageValue;
        switch (nextState) {
            case Connected:
                Log.d(TAG, "State change to connected happening...");
                lineUpByServer = false;
                currentState = nextState;
                lock.release();
                listener.onEvent(CWProtocolListener.CWPEvent.EConnected, newMessageValue);
                break;
            case Disconnected:
                Log.d(TAG, "State change to disconnected happening...");
                lineUpByServer = false;
                currentState = nextState;
                lock.release();
                try {
                    disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                listener.onEvent(CWProtocolListener.CWPEvent.EDisconnected, newMessageValue);
                break;
            case LineUp:
                Log.d(TAG, "State change to LineUp happening...");
                lineUpByServer = true;
                if (!lineUpByUser) {
                    currentState = nextState;
                    lock.release();
                    listener.onEvent(CWProtocolListener.CWPEvent.ELineUp, 0);
                } else {
                    lock.release();
                }
                break;
            case LineDown:
                if (currentState == CWPState.Connected) {
                    Log.d(TAG, "State change to LineDown happening...");
                    currentState = nextState;
                    lock.release();
                    if (currentFrequency != newMessageValue) {
                        try {
                            sendFrequency();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        listener.onEvent(CWProtocolListener.CWPEvent.EChangedFrequency, newMessageValue);
                    }
                } else {
                    lineUpByServer = false;
                    if (!lineUpByUser) {
                        currentState = nextState;
                        lock.release();
                        Log.d(TAG, "State change to LineDown happening...");
                        listener.onEvent(CWProtocolListener.CWPEvent.ELineDown, newMessageValue);
                    } else {
                        lock.release();
                    }
                }
                break;
            default:
                lock.release();
        }
    }

    private void sendMessage(int msg) throws IOException {
        if (msg != FORBIDDEN_VALUE) {
            Log.wtf(TAG, "Sending message to server: " + msg);
            outBuffer = ByteBuffer.allocate(4);
            outBuffer.order(ByteOrder.BIG_ENDIAN);
            outBuffer.putInt(msg);
            outBuffer.position(0);
            final byte[] buff = outBuffer.array();
            nos.write(buff);
            nos.flush();
            outBuffer = null;
        }
    }

    private void sendMessage(short msg) throws IOException {
        Log.wtf(TAG, "Sending message to server: " + msg);
        outBuffer = ByteBuffer.allocate(2);
        outBuffer.order(ByteOrder.BIG_ENDIAN);
        outBuffer.putShort(msg);
        outBuffer.position(0);
        final byte[] buff = outBuffer.array();
        nos.write(buff);
        nos.flush();
        outBuffer = null;
    }

    private class CWPConnectionReader extends Thread {

        private boolean running = false;
        private Runnable myProcessor = null;
        private static final String TAG = "CWPReader";

        private Socket cwpSocket = null;
        private InputStream nis = null; //Network Input Stream

        CWPConnectionReader(Runnable processor) {
            myProcessor = processor;
        }

        void startReading() {
            running = true;
            start();
        }

        void stopReading() throws InterruptedException, IOException {
            Log.wtf(TAG, "Stopping the thread");
            running = false;
            if (nos != null) {
                nos.close();
                nos = null;
            }
            if (nis != null) {
                nis.close();
                nis = null;
            }
            if (cwpSocket != null) {
                cwpSocket.close();
                cwpSocket = null;
            }
            currentState = CWPState.Disconnected;
            Log.wtf(TAG, "Thread stopped");
        }

        private void doInitialize() throws InterruptedException, IOException {
            Log.wtf(TAG, "Initializing connection");
            SocketAddress addr = new InetSocketAddress(serverAddr, serverPort);
            cwpSocket = new Socket();
            cwpSocket.connect(addr, 5000);
            sessionInitTime = System.currentTimeMillis();
            nis = cwpSocket.getInputStream();
            nos = cwpSocket.getOutputStream();
            lineUpByUser = false;
            lineUpByServer = false;
            Log.wtf(TAG, "Connection initialized");
            changeProtocolState(CWPState.Connected, 0);
        }

        @Override
        public void run() {
            try {
                doInitialize();
                // continue preparations for reading data from server
                // after preparations, read data in a while loop:
                byte[] bytes = new byte[BUFFER_LENGTH];
                ByteBuffer buffer = ByteBuffer.allocate(BUFFER_LENGTH);
                buffer.order(ByteOrder.BIG_ENDIAN);
                int bytesToRead;
                int bytesRead;
                while (running) {
                    // read stuff and handle it somehow...
                    bytesToRead = 4;
                    Log.wtf(TAG, "*Bytecount Read " + bytesToRead);
                    bytesRead = readLoop(bytes, bytesToRead);
                    if (bytesRead > 0) {
                        buffer.clear();
                        buffer.put(bytes, 0, bytesRead);
                        buffer.position(0);
                        int value = buffer.getInt();
                        if (value >= 0) {
                            changeProtocolState(CWPState.LineUp, value);
                            bytesToRead = 2;
                            bytesRead = readLoop(bytes, bytesToRead);
                            if (bytesRead > 0) {
                                buffer.clear();
                                buffer.put(bytes, 0, bytesRead);
                                buffer.position(0);
                                short shortValue = buffer.getShort();
                                changeProtocolState(CWPState.LineDown, shortValue); // value holds the frequency received from server
                            }
                        } else if (value != FORBIDDEN_VALUE) {
                            changeProtocolState(CWPState.LineDown, value); // value holds the frequency received from server
                        }
                    }
                }

            } catch (InterruptedException e) {
                Log.wtf(TAG, "Interrupted exception in thread run");
                e.printStackTrace();
            } catch (IOException e) {
                Log.wtf(TAG, "IO exception in thread run");
                e.printStackTrace();
                try {
                    Log.wtf(TAG, "Change tp disconnect because of IO exception");
                    changeProtocolState(CWPState.Disconnected, 0);
                } catch (InterruptedException e1) {
                    Log.wtf(TAG, "Interrupted exception when handling IO exception by disconnecting");
                    e1.printStackTrace();
                }
            }
            Log.wtf(TAG, "Leaving from thread.run()");
        }

        private int readLoop(byte [] bytes, int bytesToRead) throws IOException {
            int bytesRead = 0;
            do {
                Arrays.fill(bytes, (byte)0);
                int readNow = nis.read(bytes, bytesRead, bytesToRead - bytesRead);
                Log.wtf(TAG, "*Bytecount Read " + readNow);
                if (readNow == -1) { // end of stream, server closed the connection.
                    throw new IOException("Read -1 from stream"); // You should implement this too!
                } else {
                    bytesRead += readNow;
                }
            } while (bytesRead < bytesToRead);

            return bytesRead;
        }

        private void changeProtocolState(CWPState state, int param) throws InterruptedException {
            Log.wtf(TAG, "Change protocol state to " + state);
            lock.acquire();
            nextState = state;
            messageValue = param;
            receiveHandler.post(myProcessor); // <<<< tell the android.os.Handler to post an event to myProcessor.
        }
    }
}