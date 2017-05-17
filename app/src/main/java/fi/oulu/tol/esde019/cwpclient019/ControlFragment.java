package fi.oulu.tol.esde019.cwpclient019;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import fi.oulu.tol.esde019.cwpclient019.cwprotocol.CWPControl;
import fi.oulu.tol.esde019.cwpclient019.cwprotocol.CWProtocolListener;


public class ControlFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, Observer {

    CWPProvider mCWPProvider;
    CWPControl mCWPControl;
    private ToggleButton toggleConnectCWP;
    private Button btnChange;
    private EditText etChange;

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

        etChange = (EditText) v.findViewById(R.id.etChange);
        btnChange = (Button) v.findViewById(R.id.btnChange);
        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int newFrequency = Integer.parseInt(etChange.getText().toString());
                try {
                    // Get the new freq from the ControlFragment widget when button pressed to newFreq variable
                    final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                    SharedPreferences.Editor edit = sharedPref.edit();
                    edit.putString(String.valueOf(R.string.key_frequency), Integer.toString(newFrequency));
                    edit.commit();
                    mCWPControl.setFrequency(newFrequency);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

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
                final SharedPreferences sPref= PreferenceManager.getDefaultSharedPreferences(getActivity());
                String serverAddr = sPref.getString(getString(R.string.key_server_address), "");
                String addr[] = serverAddr.split(":");
                int serverPort = 0;
                if (addr.length == 2) {
                    serverPort = Integer.valueOf(addr[1]);
                    serverAddr = addr[0];
                    int frequency = Integer.valueOf(sPref.getString(getString(R.string.key_frequency), ""));
                    mCWPControl.connect(serverAddr, serverPort, frequency);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), R.string.host_address_port, Toast.LENGTH_SHORT).show();
                }

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
