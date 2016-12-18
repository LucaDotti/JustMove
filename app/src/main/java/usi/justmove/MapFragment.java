package usi.justmove;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import usi.justmove.dataAnalisys.DataAnalyzer;
import usi.justmove.dataAnalisys.Trace;
import usi.justmove.database.LocalDbController;
import usi.justmove.database.LocalSQLiteDBHelper;
import usi.justmove.utils.MoveActivity;

import static android.graphics.Color.parseColor;

//http://stackoverflow.com/questions/19353255/how-to-put-google-maps-v2-on-a-fragment-using-viewpager
//add possibility to set the current acivity by the user on each line
//add merge of lines
//add legenda
//USAGE: add also time app is running and so on..
//Datagathering: remove GPS when not necessary...
//Add sampling requency selection with slider
//add speed color adaption
//show only walking,...
//weight on the time
/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements DatePickerDialog.OnDateSetListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private MapView map;
    private GoogleMap googleMap;
    private MapFragment thisObj;

    private DataAnalyzer analyzer;
    private LocalDbController dbController;

    private Trace currentSpeedPath;
    private Map<Polyline, Marker> lines;
    private Marker currentVisibleMarker;
    private Polyline currentPolyLine;

    private TextView date;
    private DatePickerDialog datePicker;
    private LinearLayout legenda;
    private CheckBox removeErrorCheckbox;
    private CheckBox activitiesCheckbox;

    private OnFragmentInteractionListener mListener;

    public MapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
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
        thisObj = this;
        dbController = new LocalDbController(getActivity(), getActivity().getResources().getString(R.string.db_name));
        lines = new HashMap<>();
        analyzer = new DataAnalyzer();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        legenda = (LinearLayout) rootView.findViewById(R.id.map_legenda);

        map = (MapView) rootView.findViewById(R.id.googleMap);
        map.onCreate(savedInstanceState);
        map.onResume();

        removeErrorCheckbox = (CheckBox) rootView.findViewById(R.id.removeErrorCheckBox);
        activitiesCheckbox = (CheckBox) rootView.findViewById(R.id.mapFragmentActivitiesCheckbox);

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        map.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // For showing a move to my location button
                if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                googleMap.setMyLocationEnabled(true);
                LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                LatLng current = new LatLng(latitude, longitude);

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(current).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                googleMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
                    @Override
                    public void onPolylineClick(Polyline polyline) {
                        hideMarkers();
                        Marker m = lines.get(polyline);
                        if(currentPolyLine != null) {
                            currentPolyLine.setWidth(12);
                        }

                        currentVisibleMarker = m;
                        currentPolyLine = polyline;
                        List<LatLng> points = polyline.getPoints();
                        m.setPosition(points.get(points.size()/2));
                        m.setVisible(true);
                        m.showInfoWindow();
                        polyline.setWidth(20);

                    }
                });

                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        if(currentVisibleMarker != null && currentPolyLine != null) {
                            currentVisibleMarker.setVisible(false);
                            currentPolyLine.setWidth(12);
                        }
                    }
                });
            }
        });



        date = (TextView) rootView.findViewById(R.id.mapFragmentDate);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        String currentTDate = sdf.format(new Date());
        date.setText(currentTDate);
        final Calendar c = Calendar.getInstance();
        datePicker = new DatePickerDialog(getActivity(), thisObj, c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker.show();
            }
        });

//        Button b = (Button) rootView.findViewById(R.id.mapFragmentExpandButton);
//        b.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                filter.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300));
//            }
//        });
        removeErrorCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                clearMap();

                String query = buildQuery(
                        datePicker.getDatePicker().getYear(),
                        datePicker.getDatePicker().getMonth(),
                        datePicker.getDatePicker().getDayOfMonth());
                Cursor c = dbController.rawQuery(query, null);
                if(c.getCount() > 0) {
                    currentSpeedPath = analyzer.computeSpeedPath(c, 10, isChecked, 200);
                    drawPath(currentSpeedPath, true);
                    setUpSpeedLegenda(currentSpeedPath);
                } else {
                    //TODO: display no data msg
                }

                activitiesCheckbox.setEnabled(isChecked);
            }
        });

        activitiesCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                clearMap();

                if(isChecked) {
                    drawPath(currentSpeedPath, false);
                    setUpActivityLegenda(currentSpeedPath);
                } else {
                    String query = buildQuery(
                            datePicker.getDatePicker().getYear(),
                            datePicker.getDatePicker().getMonth(),
                            datePicker.getDatePicker().getDayOfMonth());
                    Cursor c = dbController.rawQuery(query, null);
                    if(c.getCount() > 0) {
                        currentSpeedPath = analyzer.computeSpeedPath(c, 10, true, 200);
                        drawPath(currentSpeedPath, true);
                        setUpSpeedLegenda(currentSpeedPath);
                    } else {
                        //TODO: display no data msg
                    }
                }

            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    private void setUpSpeedLegenda(Trace sp) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        legenda.removeAllViews();
        for(int i = 0; i < 5; i++) {
            LinearLayout item = (LinearLayout) inflater.inflate(R.layout.legenda_item, null);
            ImageView image = (ImageView) item.findViewById(R.id.legendaItemColor);
            Drawable d = ContextCompat.getDrawable(getActivity(), R.drawable.legenda_square);
            int maxSpeed = (int) sp.getMax();
            d.setTint(computeSpeedColor(maxSpeed, i*(maxSpeed/5)));
            TextView text = (TextView) item.findViewById(R.id.legendaItemText);
            text.setText(i*(maxSpeed/5) + " km/h");
            image.setImageDrawable(d);
            legenda.addView(item);
        }
//        legenda.setVisibility(View.VISIBLE);
    }

    private void setUpActivityLegenda(Trace sp) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        legenda.removeAllViews();
        legenda.invalidate();
        for(MoveActivity ac: MoveActivity.values()) {
            LinearLayout item = (LinearLayout) inflater.inflate(R.layout.legenda_item, null);
            ImageView image = (ImageView) item.findViewById(R.id.legendaItemColor);

            Drawable d = ContextCompat.getDrawable(getActivity(), R.drawable.legenda_square);
            d.setTint(computeActivityColor(ac));
            image.setImageDrawable(d);
            TextView text = (TextView) item.findViewById(R.id.legendaItemText);
            text.setText(ac.toString().toLowerCase());

            legenda.addView(item);
        }
//        legenda.setVisibility(View.VISIBLE);
    }

    private void clearMap() {
        for(Map.Entry<Polyline, Marker> entry : lines.entrySet()) {
            Polyline line = entry.getKey();
            Marker marker = entry.getValue();
            line.remove();
            marker.remove();
        }
        lines.clear();
    }

    private void hideMarkers() {
        for(Map.Entry<Polyline, Marker> entry : lines.entrySet()) {
            Marker marker = entry.getValue();
            marker.setVisible(false);
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        clearMap();

        String query = buildQuery(year, month, dayOfMonth);
        Cursor c = dbController.rawQuery(query, null);
        if(c.getCount() > 0) {
            currentSpeedPath = analyzer.computeSpeedPath(c, 10, removeErrorCheckbox.isChecked(), 200);
            drawPath(currentSpeedPath, true);
            setUpSpeedLegenda(currentSpeedPath);
        } else {
            //TODO: display no data msg
        }
        removeErrorCheckbox.setEnabled(true);
    }



    private String buildQuery(int year, int month, int dayOfMonth) {
        month++;
        String dateString = Integer.toString(year) + "-" + Integer.toString(month) + "-" + Integer.toString(dayOfMonth);
        date.setText(dateString.toCharArray(), 0, dateString.length());
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime dt = new DateTime(year, month, dayOfMonth, 0, 0, 0);
        long dayStart = dt.getMillis();
        dt = new DateTime(year, month, dayOfMonth, 23, 59, 59);
        long dayEnd = dt.getMillis();
        return "SELECT * FROM " + LocalSQLiteDBHelper.TABLE_LOCATION +
                " WHERE " + LocalSQLiteDBHelper.KEY_LOCATION_TIMESTAMP + " BETWEEN " + dayStart + " AND " + dayEnd;
    }

    private void drawPath(Trace sp, boolean isSpeedPath) {
        List<Integer> speed = sp.getSpeeds();
        List<Long> times = sp.getSubPathTimes();
        List<MoveActivity> activities = sp.getActivitiesPath();

        Iterator<List<LatLng>> it = sp.getPath().iterator();

        int i = 0;
        while(it.hasNext()) {
            List<LatLng> subPath = it.next();

            int color;
            if(isSpeedPath) {
                color = computeSpeedColor(sp.getMax(), speed.get(i));
            } else {
                color = computeActivityColor(activities.get(i));
            }

//            System.out.println(color);
//            System.out.println(activities.get(i));
            Polyline line = googleMap.addPolyline(new PolylineOptions()
                    .addAll(subPath)
                    .width(12)
                    .color(color)
                    .geodesic(true)
                    .clickable(true)
            );

            //        BitmapDescriptor transparent = BitmapDescriptorFactory.fromResource(R.drawable.transparent_bitmap);

            String text;
            String title;
            if(isSpeedPath) {
                text = Integer.toString(speed.get(i)) + " km/h " + times.get(i) + " s";
                title = "Speed and time";
            } else {
                text = activities.get(i).toString();
                title = "Activity";
            }
            MarkerOptions markerOptions = new MarkerOptions()
                    .snippet(text)
//                .anchor((float) 0.5, (float) 0.5)
                    .title(title)
                    .position(new LatLng(0,0));

//                .icon(map);
            Marker marker = googleMap.addMarker(markerOptions);
            marker.setVisible(false);

            lines.put(line, marker);
            i++;
        }

    }

    private int computeSpeedColor(int maxSpeed,  int speed) {
        if(speed > maxSpeed) {
            speed = maxSpeed;
        }

        int s = (int) speed;

        int maxColor = 255;

        int b = Math.abs((s*maxColor)/maxSpeed - maxColor);
        int r = (s*maxColor)/maxSpeed;
        return parseColor(String.format("#%02x%02x%02x", r, 0, b));
    }

    private int computeActivityColor(MoveActivity activity) {
        System.out.println(activity);
        switch(activity) {
            case STATIONARY: return ResourcesCompat.getColor(getResources(), R.color.activityStationary, null);
            case WALKING: return ResourcesCompat.getColor(getResources(), R.color.activityWalking, null);
            case BICYCLING: return ResourcesCompat.getColor(getResources(), R.color.activityBicycling, null);
            case DRIVING: return ResourcesCompat.getColor(getResources(), R.color.activityDriving, null);
            case FLYING: return ResourcesCompat.getColor(getResources(), R.color.activityFlying, null);
            default: return Color.parseColor("#000000");
        }
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

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        map.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        map.onLowMemory();
    }
}