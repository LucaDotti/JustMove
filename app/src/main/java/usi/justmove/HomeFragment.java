package usi.justmove;
import android.net.Uri;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;

import usi.justmove.R;
import usi.justmove.database.LocalDbController;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends android.support.v4.app.Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button buttonDisagree;
    private Button buttonAgree;

    private LocalDbController dbController;

    private OnFragmentInteractionListener mListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {

        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        String check = "";

        try {

            dbController = new LocalDbController(context, "JustMove");
            dbController.initialize();
            check = dbController.getIfAgreed();

        } catch (Exception e){
            e.printStackTrace();
        }

        System.out.println("Check if true: " + check);

        if (check.equals("1")){
            View view = inflater.inflate(R.layout.home_agree, container, false);

            PieChart chart = (PieChart) view.findViewById(R.id.chart);

            ArrayList<PieEntry> entries = new ArrayList<>();

            // enter data
            entries.add(new PieEntry(18.5f, "Red"));
            entries.add(new PieEntry(26.7f, "Green"));
            entries.add(new PieEntry(24.0f, "Magenta"));
            entries.add(new PieEntry(30.8f, "Yellow"));

            PieDataSet set = new PieDataSet(entries, "Status");
            set.setColors(new int[] { Color.RED, Color.GREEN, Color.MAGENTA, Color.YELLOW });

            PieData data = new PieData(set);
            chart.setData(data);
            chart.setHoleColor(Color.GRAY);


            return view;
        }


        View view = inflater.inflate(R.layout.fragment_home, container, false);

        buttonDisagree = (Button) view.findViewById(R.id.disagree);

        buttonDisagree.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                clickButtonDisagree();
            }
        });

        buttonAgree = (Button) view.findViewById(R.id.agree);

        buttonAgree.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                clickButtonAgree();
            }
        });


        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event

    /**
     * If user disagrees, exit application
     */
    public void clickButtonDisagree() {
        getActivity().finish();
    }

    /**
     * Bussines login on agree button:
     * Insert into database information that the user has agreed
     * to term & condition
     */
    public void clickButtonAgree() {

        try {

            dbController = new LocalDbController(context, "JustMove");
            dbController.insertControlTrue();
            restart();

        } catch (Exception e){
            e.printStackTrace();
        }


    }

    /**
     * Method restarts application
     */
    public void restart(){
        Intent i = context.getPackageManager()
                .getLaunchIntentForPackage( context.getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }




    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}