package fi.oulu.tol.esde019.cwpclient019;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import fi.oulu.tol.esde019.cwpclient019.cwprotocol.CWPControl;
import fi.oulu.tol.esde019.cwpclient019.cwprotocol.CWProtocolListener;
import fi.oulu.tol.esde019.cwpclient019.model.CWPModel;


public class ControlFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, Observer {

    CWPProvider mCWPProvider;
    CWPControl mCWPControl;
    private ToggleButton toggleConnectCWP;

    public ControlFragment() {
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
        View v = inflater.inflate(R.layout.fragment_control, container, false);

        toggleConnectCWP = (ToggleButton) v.findViewById(R.id.toggleConnectCWP);
        toggleConnectCWP.setOnCheckedChangeListener(this);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCWPProvider = (CWPProvider) getActivity();
        mCWPControl = mCWPProvider.getControl();
        mCWPControl.addObserver(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCWPControl.deleteObserver(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (isChecked) {
            try {
                mCWPControl.connect("serverAddr", 2, 2);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Connection error", Toast.LENGTH_SHORT).show();
            }
        } else {
            try {
                mCWPControl.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Connection error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data == CWProtocolListener.CWPEvent.EConnected) {
            Toast.makeText(getActivity(), getString(R.string.toggle_connected), Toast.LENGTH_SHORT).show();
        }
        if (data == CWProtocolListener.CWPEvent.EDisconnected) {
            Toast.makeText(getActivity(), getString(R.string.toggle_disconnected), Toast.LENGTH_SHORT).show();
        }
    }
}
