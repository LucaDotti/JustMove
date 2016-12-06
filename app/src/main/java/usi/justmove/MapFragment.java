package usi.justmove;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.session.IMediaControllerCallback;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Date;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.text.Line;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import usi.justmove.database.LocalDbController;
import usi.justmove.database.LocalSQLiteDBHelper;

import static android.R.color.transparent;
import static junit.runner.Version.id;

//http://stackoverflow.com/questions/19353255/how-to-put-google-maps-v2-on-a-fragment-using-viewpager
//add possibility to set the current acivity by the user on each line
//add merge of lines
//add legenda
//USAGE: add also time app is running and so on..
//Datagathering: remove GPS when not necessary...
//Add sampling requency selection with slider
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
    private TextView date;
    private MapFragment thisObj;
    private LocalDbController dbController;
    private Map<Polyline, Marker> lines;
    private LinearLayout legenda;
    private Marker currentVisibleMarker;
    private Polyline currentPolyLine;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        legenda = (LinearLayout) rootView.findViewById(R.id.map_legenda);
        map = (MapView) rootView.findViewById(R.id.googleMap);
        map.onCreate(savedInstanceState);

        map.onResume();

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
                // For dropping a marker at a point on the Map
                LatLng current = new LatLng(latitude, longitude);
                googleMap.addMarker(new MarkerOptions().position(current).title("Marker Title").snippet("Marker Description"));

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

        setUpLegenda(legenda);



        date = (TextView) rootView.findViewById(R.id.mapFragmentDate);

        final Calendar c = Calendar.getInstance();
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getActivity(), thisObj, c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    private void setUpLegenda(LinearLayout legenda) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(int i = 0; i < 5; i++) {
            LinearLayout item = (LinearLayout) inflater.inflate(R.layout.legenda_item, null);
            ImageView image = (ImageView) item.findViewById(R.id.legendaItemColor);
            Drawable d = ContextCompat.getDrawable(getActivity(), R.drawable.legenda_square);
//            d.setColorFilter(computeColor(i*50), PorterDuff.Mode.SRC_IN);
            d.setTint(computeColor(i*50));
            TextView text = (TextView) item.findViewById(R.id.legendaItemText);
            text.setText(i*50 + "km/h");
            legenda.addView(item);
        }


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
        month++;
        String dateString = Integer.toString(year) + "-" + Integer.toString(month) + "-" + Integer.toString(dayOfMonth);
        date.setText(dateString.toCharArray(), 0, dateString.length());
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime dt = new DateTime(year, month, dayOfMonth, 0, 0, 0);
        String dayStart = dtfOut.print(dt);
        long dS = dt.getMillis();
        dt = new DateTime(year, month, dayOfMonth, 23, 59, 59);
        String dayEnd = dtfOut.print(dt);
        long dE = dt.getMillis();
        String query = buildQuery(Long.toString(dS), Long.toString(dE));
        List<LatLng> points = new ArrayList<>();
        Cursor c = dbController.rawQuery(query, null);
        if(c.getCount() > 0) {
            drawSpeedPath(c);
        }

//        LatLng currPoint = null;
//        while(c.moveToNext()) {
//            currPoint = new LatLng(c.getDouble(3), c.getDouble(4));
//            points.add(currPoint);
//        }
//
//        drawPath(points);
    }

    private void drawSpeedPath(Cursor c) {
        List<LatLng> currPath = new ArrayList<>();
        double currSpeed = 0;
        double tempSpeed = 0;
        LatLng currPoint;
        c.moveToNext();
        LatLng prevPoint = new LatLng(c.getDouble(3), c.getDouble(4));
        currPath.add(prevPoint);
        c.moveToNext();

        currPoint = new LatLng(c.getDouble(3), c.getDouble(4));

        currSpeed = computeSpeed(prevPoint, currPoint);

        while(c.moveToNext()) {
            prevPoint = currPoint;
            currPoint = new LatLng(c.getDouble(3), c.getDouble(4));
            tempSpeed = computeSpeed(prevPoint, currPoint);
            if(tempSpeed >= currSpeed-10 && tempSpeed <= currSpeed+10) {
                currPath.add(currPoint);
            } else {
                drawPath(currPath, computeColor(currSpeed), currSpeed);
                currPath.clear();
                currPath.add(prevPoint);
                currPath.add(currPoint);
                currSpeed = tempSpeed;
            }
        }
    }

    private int computeColor(double speed) {
        if(speed > 200) {
            speed = 200;
        }
        int maxSpeed = 200;
        int s = (int) speed;

        int maxColor = 255;

        int b = Math.abs((s*maxColor)/maxSpeed - maxColor);
        int r = (s*maxColor)/maxSpeed;
        return Color.parseColor(String.format("#%02x%02x%02x", r, 0, b));
    }

    /**
     * Returns speed between two points in m/s
     * @param start
     * @param end
     * @return
     */
    private double computeSpeed(LatLng start, LatLng end) {
        Location l1 = new Location("");
        l1.setLatitude(start.latitude);
        l1.setLongitude(start.longitude);
        Location l2 = new Location("");
        l2.setLatitude(end.latitude);
        l2.setLongitude(end.longitude);

        double distance = l1.distanceTo(l2);

        return (distance/1000)*3600;
    }

    private void drawPath(List<LatLng> points, int color, double speed) {
        Polyline line = googleMap.addPolyline(new PolylineOptions()
                .addAll(points)
                .width(12)
                .color(color)
                .geodesic(true)
                .clickable(true)
        );
//        BitmapDescriptor transparent = BitmapDescriptorFactory.fromResource(R.drawable.transparent_bitmap);

        MarkerOptions markerOptions = new MarkerOptions()
                .snippet(Integer.toString((int) speed) + " km/h")
//                .anchor((float) 0.5, (float) 0.5)
                .position(new LatLng(0,0))
                .title(getActivity(speed));
//                .icon(map);

        Marker marker = googleMap.addMarker(markerOptions);
        marker.setVisible(false);

        lines.put(line, marker);
    }

    private String getActivity(double speed) {
        if(speed <= 3) {
            return "Stationary";
        } else if (speed > 3 && speed <= 9) {
            return "Walking";
        } else if (speed > 9 && speed <= 30) {
            return "Bicyling";
        } else if (speed > 30 && speed <= 170) {
            return "Driving";
        } else {
            return "Flying";
        }
    }

    private String buildQuery(String dayStart, String dayEnd) {
        return "SELECT * FROM " + LocalSQLiteDBHelper.TABLE_LOCATION +
                " WHERE " + LocalSQLiteDBHelper.KEY_LOCATION_TIMESTAMP + " BETWEEN " + dayStart + " AND " + dayEnd;
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