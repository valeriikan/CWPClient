package fi.oulu.tol.esde019.cwpclient019.model;

import android.media.AudioManager;
import android.media.ToneGenerator;

import java.util.Observable;
import java.util.Observer;

import fi.oulu.tol.esde019.cwpclient019.cwprotocol.CWProtocolListener;

public class Signaller implements Observer {

    private ToneGenerator tone;

    @Override
    public void update(Observable observable, Object o) {
        if (o == CWProtocolListener.CWPEvent.ELineUp) {
            start();
        } else {
            stop();
        }
    }

    public void start() {
        tone = new ToneGenerator(AudioManager.STREAM_DTMF, ToneGenerator.MAX_VOLUME);
        tone.startTone(AudioManager.STREAM_DTMF);
    }

    public void stop() {
        if (tone != null) {
            tone.stopTone();
        }
    }
}