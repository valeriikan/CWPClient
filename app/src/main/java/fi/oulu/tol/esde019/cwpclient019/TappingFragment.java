package fi.oulu.tol.esde019.cwpclient019;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import fi.oulu.tol.esde019.cwpclient019.model.CWPModel;


public class TappingFragment extends Fragment implements View.OnTouchListener, Observer {

    CWPMessaging mCWPMessaging;
    CWPProvider mCWPProvider;
    private ImageView imageView;

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
        View v = inflater.inflate(R.layout.fragment_tapping, container, false);

        imageView = (ImageView) v.findViewById(R.id.imageView);
        imageView.setOnTouchListener(this);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCWPProvider = (CWPProvider) getActivity();
        mCWPMessaging = mCWPProvider.getMessaging();
        mCWPMessaging.addObserver(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCWPMessaging.deleteObserver(this);
        mCWPMessaging = null;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                try {
                    mCWPMessaging.lineUp();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Connection error", Toast.LENGTH_SHORT).show();
                }
                break;
            case MotionEvent.ACTION_UP:
                try {
                    mCWPMessaging.lineDown();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Connection error", Toast.LENGTH_SHORT).show();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                //imageView.setImageResource(R.mipmap.hal9000_down);
                break;
        }
        return true;
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data == CWPModel.CWPState.LineUp) {
            Toast.makeText(getActivity(), "LineUp", Toast.LENGTH_SHORT).show();
            imageView.setImageResource(R.mipmap.hal9000_up);
        }
        if (data == CWPModel.CWPState.LineDown) {
            Toast.makeText(getActivity(), "LineDown", Toast.LENGTH_SHORT).show();
            imageView.setImageResource(R.mipmap.hal9000_down);
        }
    }
}
