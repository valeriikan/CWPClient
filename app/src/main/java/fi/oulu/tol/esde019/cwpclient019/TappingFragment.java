package fi.oulu.tol.esde019.cwpclient019;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import fi.oulu.tol.esde019.cwpclient019.cwprotocol.CWPMessaging;
import fi.oulu.tol.esde019.cwpclient019.cwprotocol.CWProtocolListener;


public class TappingFragment extends Fragment implements View.OnTouchListener, Observer {

    private CWPMessaging mCWPMessaging;
    private final static String TAG = "CWP019";

    public TappingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tapping, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateView(getView());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCWPMessaging.deleteObserver(this);
        mCWPMessaging = null;
    }

    @Override
    public void update(Observable observable, Object data) {
        ImageView imageView = (ImageView) getView().findViewById(R.id.imageView);
        if (imageView != null) {
            if (data.equals(CWProtocolListener.CWPEvent.ELineUp)) {
                imageView.setImageResource(R.mipmap.hal9000_up);
            } else if (data.equals(CWProtocolListener.CWPEvent.ELineDown) ||
                    data.equals(CWProtocolListener.CWPEvent.EConnected)) {
                imageView.setImageResource(R.mipmap.hal9000_down);
            } else if (data.equals(CWProtocolListener.CWPEvent.EDisconnected)) {
                imageView.setImageResource(R.mipmap.hal9000_offline);
            }
        }
        updateView(getView());
    }

    public void setMessaging(CWPMessaging msg) {
        mCWPMessaging = msg;
        mCWPMessaging.addObserver(this);
        updateView(getView());
    }

    public void updateView(View view) {
        Log.wtf(TAG, "Updating views");
        if (view != null) {
            ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
            imageView.setOnTouchListener(this);
            if (mCWPMessaging != null) {
                if (mCWPMessaging.lineIsUp()) {
                    imageView.setImageResource(R.mipmap.hal9000_up);
                } else if (mCWPMessaging.isConnected()) {
                    imageView.setImageResource(R.mipmap.hal9000_down);
                } else {
                    imageView.setImageResource(R.mipmap.hal9000_offline);
                }
            }
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (mCWPMessaging != null) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    try {
                        mCWPMessaging.lineUp();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), R.string.connection_error, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    try {
                        mCWPMessaging.lineDown();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), R.string.connection_error, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
        return true;
    }
}