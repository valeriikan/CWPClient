package fi.oulu.tol.esde019.cwpclient019;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import fi.oulu.tol.esde019.cwpclient019.cwprotocol.CWPControl;
import fi.oulu.tol.esde019.cwpclient019.cwprotocol.CWProtocolListener;


public class ControlFragment extends Fragment implements View.OnClickListener, Observer {

    private CWPControl mCWPControl;
    private final static String TAG = "CWP019";

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
        return inflater.inflate(R.layout.fragment_control, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateView(getView());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCWPControl.deleteObserver(this);
        mCWPControl = null;
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data.equals(CWProtocolListener.CWPEvent.EConnected)) {
            Toast.makeText(getActivity(), getString(R.string.status_connected), Toast.LENGTH_SHORT).show();
        } else if (data.equals(CWProtocolListener.CWPEvent.EDisconnected)) {
            Toast.makeText(getActivity(), getString(R.string.status_disconnected), Toast.LENGTH_SHORT).show();
        } else if (data.equals(CWProtocolListener.CWPEvent.EChangedFrequency)) {
            int frequency = mCWPControl.frequency();
            final String msg = getString(R.string.status_frequency) + frequency;
            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
        }
        updateView(getView());
    }

    public void setControl(CWPControl cntrl) {
        mCWPControl = cntrl;
        mCWPControl.addObserver(this);
        updateView(getView());
    }

    public void updateView(View view) {
        if (view != null){
            Log.wtf(TAG, "Updating views");
            final ToggleButton btnConnect = (ToggleButton) view.findViewById(R.id.btnConnect);
            final Button btnChange = (Button) view.findViewById(R.id.btnChange);
            final EditText etChange = (EditText) view.findViewById(R.id.etChange);
            final SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            btnConnect.setOnClickListener(this);
            btnChange.setOnClickListener(this);

            if (mCWPControl != null) {
                btnConnect.setEnabled(true);
                btnChange.setEnabled(true);
                if (mCWPControl.isConnected()) {
                    btnConnect.setChecked(true);
                    etChange.setText(Integer.toString(mCWPControl.frequency()));
                } else {
                    btnConnect.setChecked(false);
                    etChange.setText(sPref.getString(getString(R.string.key_frequency), "1"));
                }
            } else { // no obj available
                btnConnect.setEnabled(false);
                btnConnect.setChecked(false);
                btnChange.setEnabled(false);
                etChange.setText(sPref.getString(getString(R.string.key_frequency), "1"));
            }
        }
    }

    @Override
    public void onClick(View view) {
        final EditText etChange = (EditText) getView().findViewById(R.id.etChange);
        if (view.getId() == R.id.btnChange) {
            String strFrequency = etChange.getText().toString();
            if (strFrequency.length() > 0) {
                try {
                    int newFrequency = Integer.valueOf(strFrequency);
                    int curFrequency = mCWPControl.frequency();
                    if (newFrequency != curFrequency) {
                        try {
                            // Get the new freq from the ControlFragment widget when button pressed to newFrequency variable
                            mCWPControl.setFrequency(newFrequency);
                            final SharedPreferences sharedPref = PreferenceManager.
                                    getDefaultSharedPreferences(getActivity().getApplicationContext());
                            SharedPreferences.Editor edit = sharedPref.edit();
                            edit.putString(String.valueOf(R.string.key_frequency), Integer.toString(newFrequency));
                            edit.commit();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(),
                                R.string.frequency_same, Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity().getApplicationContext(),
                            R.string.frequency_wrong, Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(getActivity().getApplicationContext(),
                        R.string.frequency_empty, Toast.LENGTH_SHORT).show();
            }
        } else if (view.getId() == R.id.btnConnect) {
            if (((ToggleButton) view).isChecked()) {
                Toast.makeText(getActivity().getApplicationContext(),
                        getString(R.string.status_connecting), Toast.LENGTH_SHORT).show();
                try {
                    if (mCWPControl != null) {
                        final SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        String serverAddr = sPref.getString(getString(R.string.key_server_address), "");
                        String addr[] = serverAddr.split(":");
                        int serverPort = 0;
                        if (addr.length == 2) {
                            try {
                                serverAddr = addr[0];
                                serverPort = Integer.valueOf(addr[1]);
                                int frequency = Integer.valueOf(sPref.getString(getString(R.string.key_frequency), ""));
                                mCWPControl.connect(serverAddr, serverPort, frequency);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                Toast.makeText(getActivity().getApplicationContext(),
                                        R.string.settings_wrong, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(),
                                    R.string.host_address_port, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(),
                                R.string.connection_error, Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), R.string.connection_error, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity().getApplicationContext(),
                        getString(R.string.status_disconnecting), Toast.LENGTH_SHORT).show();
                if (mCWPControl != null) {
                    try {
                        mCWPControl.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), R.string.connection_error, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}