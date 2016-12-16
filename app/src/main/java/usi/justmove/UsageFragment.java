package usi.justmove;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.games.snapshot.Snapshot;

import android.os.Handler;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import usi.justmove.usage.UsageSnapshot;
import usi.justmove.usage.WifiUsage;

import static android.R.attr.data;
import static android.R.attr.entries;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UsageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UsageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UsageFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private LineChart memoryChart;
    private LineChart wifiChart;
    private Handler handler = new Handler();
    private Timer timer = new Timer();
    private UsageSnapshot snapshot;
    private long currentT;

    public UsageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UsageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UsageFragment newInstance(String param1, String param2) {
        UsageFragment fragment = new UsageFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_usage, container, false);
        currentT = 0;
        memoryChart = (LineChart) rootView.findViewById(R.id.usageMemoryChart);
        wifiChart = (LineChart) rootView.findViewById(R.id.usageWifiChart);

        snapshot = new UsageSnapshot(getContext());

        initMemoryChart(memoryChart);
        initWifiChart(wifiChart);

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                addMemoryEntry(memoryChart);

                addWifiEntry(wifiChart);

                currentT++;
            }
        };

        new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {
                    getActivity().runOnUiThread(runnable);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }
        }).start();

        return rootView;
    }

    private void initMemoryChart(LineChart chart) {
        snapshot.takeSnaphot();
        long[] memValue = new long[1];
        memValue[0] = snapshot.getMemoryUsage();
        List<Entry> entries = new ArrayList<Entry>();

        entries.add(new Entry(0, memValue[0]));

        LineDataSet dataSet = new LineDataSet(entries, "Label");
        dataSet.setColor(R.color.white);
        dataSet.setValueTextColor(R.color.white);
        dataSet.setDrawValues(false);
        LineData lineData = new LineData(dataSet);

        memoryChart.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.GeneralBackground, null));
        memoryChart.setDrawGridBackground(false);
        memoryChart.getXAxis().setEnabled(false);
        memoryChart.getAxisLeft().setTextColor(ResourcesCompat.getColor(getResources(), R.color.white, null));
        memoryChart.getAxisRight().setEnabled(false);
        Description desc = new Description();
        desc.setTextColor(ResourcesCompat.getColor(getResources(), R.color.white, null));
        desc.setText("mb");
        memoryChart.setDescription(desc);
        memoryChart.getLegend().setEnabled(false);
        memoryChart.setData(lineData);
        memoryChart.invalidate();
    }

    private void initWifiChart(LineChart chart) {
        snapshot.takeSnaphot();
        WifiUsage wifi = snapshot.getWifiUsage();
        double[] wifiTxValue = new double[1];
        wifiTxValue[0] = wifi.getTxUsage();

        double[] wifiRxValue = new double[1];
        wifiRxValue[0] = wifi.getRxUsage();

        List<Entry> txEntries = new ArrayList<Entry>();
        List<Entry> rxEntries = new ArrayList<Entry>();
        txEntries.add(new Entry(0, (float) wifiTxValue[0]));
        rxEntries.add(new Entry(0, (float) wifiRxValue[0]));

        LineDataSet txDataSet = new LineDataSet(txEntries, "Transmission");
        txDataSet.setColor(R.color.red);
        txDataSet.setDrawValues(false);
        LineDataSet rxDataSet = new LineDataSet(rxEntries, "Reception");
        rxDataSet.setColor(R.color.blue);
        rxDataSet.setDrawValues(false);
        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(txDataSet);
        dataSets.add(rxDataSet);

        LineData lineData = new LineData(dataSets);

        wifiChart.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.GeneralBackground, null));
        wifiChart.setDrawGridBackground(false);
        wifiChart.getXAxis().setEnabled(false);
        wifiChart.getAxisLeft().setTextColor(ResourcesCompat.getColor(getResources(), R.color.white, null));
        wifiChart.getAxisRight().setEnabled(false);
        Description desc = new Description();
        desc.setTextColor(ResourcesCompat.getColor(getResources(), R.color.white, null));
        desc.setText("mb");
        wifiChart.setDescription(desc);
        wifiChart.getLegend().setTextColor(ResourcesCompat.getColor(getResources(), R.color.white, null));
        wifiChart.setData(lineData);
        wifiChart.invalidate();
    }

    private void addMemoryEntry(LineChart chart) {
        snapshot.takeSnaphot();
        chart.getLineData().addEntry(new Entry(currentT, snapshot.getMemoryUsage()), 0);
        chart.setVisibleXRangeMaximum(10);
        chart.moveViewToX(chart.getLineData().getEntryCount());
        memoryChart.getLineData().notifyDataChanged();
        memoryChart.notifyDataSetChanged(); // let the chart know it's data changed
        memoryChart.invalidate();
    }

    private void addWifiEntry(LineChart chart) {
        snapshot.takeSnaphot();
        chart.setVisibleXRangeMaximum(10);
        chart.moveViewToX(chart.getLineData().getEntryCount());
        chart.getLineData().getDataSetByLabel("Transmission", true).addEntry(new Entry(currentT, (long) snapshot.getWifiUsage().getTxUsage()));
        chart.getLineData().getDataSetByLabel("Reception", true).addEntry(new Entry(currentT, (long) snapshot.getWifiUsage().getRxUsage()));
        wifiChart.getLineData().notifyDataChanged();
        wifiChart.notifyDataSetChanged(); // let the chart know it's data changed
        wifiChart.invalidate();
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

//    class RealTimeUsageData extends AsyncTask<LineChart, Void, Void> {
//
//        @Override
//        protected Void doInBackground(LineChart... params) {
//            snapshot.takeSnaphot();
//            for(int i = 0; i < params.length; i++) {
////                params[i].getLineData().addEntry(new Entry(currentT, snapshot.getMemoryUsage()), (int) currentT);
//            }
//            return null;
//        }
//    }

}
//
