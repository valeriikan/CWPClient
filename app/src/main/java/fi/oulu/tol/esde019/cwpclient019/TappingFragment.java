package fi.oulu.tol.esde019.cwpclient019;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class TappingFragment extends Fragment {

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
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        imageView.setImageResource(R.mipmap.hal9000_up);
                        break;
                    case MotionEvent.ACTION_UP:
                        imageView.setImageResource(R.mipmap.hal9000_down);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        imageView.setImageResource(R.mipmap.hal9000_down);
                        break;
                }
                return true;
            }
        });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
