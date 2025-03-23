package com.example.e2046073_assignment02;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TripActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {


    private MapView mapView;
    private List<DropOffPoint> dropOffPointsList;
    private ArrayAdapter<DropOffPoint> dropOffPointsAdapter;
    private SQLiteDatabase database;
    private GoogleMap googleMap;
    private LocationManager locationManager;
    private Marker currentLocationMarker;
    private Button deliveryStatusButton;
    private Button endTripButton;
    private Marker selectedMarker;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        context = this;

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        dropOffPointsList = new ArrayList<>();
        dropOffPointsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dropOffPointsList);

        retrieveDropOffPoints();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        deliveryStatusButton = findViewById(R.id.button_delivery_status);
        endTripButton = findViewById(R.id.button_end_trip);

        deliveryStatusButton.setEnabled(false);
        endTripButton.setEnabled(false);
        DatabaseHelper DatabaseHelper = new DatabaseHelper(this);

        deliveryStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdateDeliveryStatusWindow();
            }

        });


    }




    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        showDropOffPointsOnMap();
        requestLocationUpdates();
        this.googleMap = googleMap;

        // Set up the map settings and properties

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String[] columns = {
                DatabaseHelper.COLUMN_NAME,
                DatabaseHelper.COLUMN_DELIVERY_STATUS,
                DatabaseHelper.COLUMN_LATITUDE,
                DatabaseHelper.COLUMN_LONGITUDE
        };
        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, columns, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME);
            int deliveryStatusIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DELIVERY_STATUS);
            int latitudeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_LATITUDE);
            int longitudeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_LONGITUDE);

            do {
                String name = cursor.getString(nameIndex);
                String deliveryStatus = cursor.getString(deliveryStatusIndex);
                double latitude = cursor.getDouble(latitudeIndex);
                double longitude = cursor.getDouble(longitudeIndex);

                LatLng location = new LatLng(latitude, longitude);

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(location)
                        .title(name);

                // Check if delivery status is "Delivered" and set marker color to green
                if (deliveryStatus != null && deliveryStatus.equalsIgnoreCase("Delivered")) {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                }

                googleMap.addMarker(markerOptions);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();

        // Set the map focus to the current location
        if (currentLocationMarker != null) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(currentLocationMarker.getPosition());

            db = databaseHelper.getReadableDatabase();
            cursor = db.query(DatabaseHelper.TABLE_NAME, columns, null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME);
                int deliveryStatusIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DELIVERY_STATUS);
                int latitudeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_LATITUDE);
                int longitudeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_LONGITUDE);

                do {
                    String name = cursor.getString(nameIndex);
                    String deliveryStatus = cursor.getString(deliveryStatusIndex);
                    double latitude = cursor.getDouble(latitudeIndex);
                    double longitude = cursor.getDouble(longitudeIndex);

                    LatLng location = new LatLng(latitude, longitude);
                    builder.include(location);

                    // Check if delivery status is "Delivered" and set marker color to green
                    if (deliveryStatus != null && deliveryStatus.equalsIgnoreCase("Delivered")) {
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(location)
                                .title(name)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

                        googleMap.addMarker(markerOptions);
                    }
                } while (cursor.moveToNext());
            }

            LatLngBounds bounds = builder.build();

            // Set padding around the markers and current location
            int paddingInPixels = 50; // Adjust the padding value as needed
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, paddingInPixels);
            googleMap.moveCamera(cameraUpdate);

            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (!marker.getTitle().equals("Your Current Location")) {
                    selectedMarker = marker;
                    deliveryStatusButton.setEnabled(true);
                } else {
                    selectedMarker = null;
                    deliveryStatusButton.setEnabled(false);
                }
                endTripButton.setEnabled(true);

                if (marker.equals(currentLocationMarker)) {
                    // Show custom info window for the current location marker
                    googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                        @Override
                        public View getInfoWindow(Marker marker) {
                            return null;
                        }

                        @Override
                        public View getInfoContents(Marker marker) {
                            return createInfoWindowView(marker);
                        }
                    });

                    marker.showInfoWindow(); // Show the info window for the current location marker
                    return true;
                }

                return false;
            }
        });

    }

    public View createInfoWindowView(Marker marker) {
        View view = getLayoutInflater().inflate(R.layout.marker_info_window, null);

        TextView textName = view.findViewById(R.id.text_name);
        TextView textSubscriberId = view.findViewById(R.id.text_subscriber_id);
        TextView textAddress = view.findViewById(R.id.text_address);
        TextView textPhone = view.findViewById(R.id.text_phone);
        TextView textPaperTypes = view.findViewById(R.id.text_paper_types);
        TextView textDeliveryStatus = view.findViewById(R.id.text_delivery_Status);
        TextView textRenewalReminderIssued = view.findViewById(R.id.text_renewalReminderIssued);
        TextView textDeliveryHistory = view.findViewById(R.id.text_deliveryHistory);

        DropOffPoint selectedDropOffPoint = null;
        if (marker.equals(currentLocationMarker)) {
            textName.setText("Your Current Location");
        } else {
            DatabaseHelper databaseHelper = new DatabaseHelper(this);
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            String[] columns = {
                    DatabaseHelper.COLUMN_ID,
                    DatabaseHelper.COLUMN_NAME,
                    DatabaseHelper.COLUMN_ADDRESS,
                    DatabaseHelper.COLUMN_PHONE,
                    DatabaseHelper.COLUMN_PAPER_TYPES,
                    DatabaseHelper.COLUMN_DELIVERY_STATUS,
                    DatabaseHelper.COLUMN_RENEWAL_REMINDER_ISSUED,
                    DatabaseHelper.COLUMN_DELIVERY_HISTORY
            };
            String selection = DatabaseHelper.COLUMN_NAME + " = ?";
            String[] selectionArgs = { marker.getTitle() };
            Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, columns, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
                int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME);
                int addressIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ADDRESS);
                int phoneIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PHONE);
                int paperTypesIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PAPER_TYPES);
                int deliveryStatusIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DELIVERY_STATUS);
                int renewalReminderIssuedIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_RENEWAL_REMINDER_ISSUED);
                int deliveryHistoryIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DELIVERY_HISTORY);

                if (idIndex >= 0 && nameIndex >= 0 && addressIndex >= 0 && phoneIndex >= 0 && paperTypesIndex >= 0 && deliveryStatusIndex >= 0 && renewalReminderIssuedIndex >= 0 && deliveryHistoryIndex >= 0) {
                    String id = cursor.getString(idIndex);
                    String name = cursor.getString(nameIndex);
                    String address = cursor.getString(addressIndex);
                    String phone = cursor.getString(phoneIndex);
                    String paperTypes = cursor.getString(paperTypesIndex);
                    String deliveryStatus = cursor.getString(deliveryStatusIndex);
                    int renewalReminderIssued = cursor.getInt(renewalReminderIssuedIndex);
                    String deliveryHistory = cursor.getString(deliveryHistoryIndex);

                    selectedDropOffPoint = new DropOffPoint(
                            0.0, // Set the initial values for latitude and longitude, you may update them based on your data
                            0.0,
                            stringToPaperTypes(paperTypes),
                            deliveryStatus,
                            "",
                            false,
                            renewalReminderIssued == 1,
                            "",
                            id,
                            name,
                            address,
                            phone,
                            stringToDeliveryHistory(deliveryHistory)
                    );
                    textName.setText(marker.getTitle());
                    textSubscriberId.setText("Subscriber ID: " + selectedDropOffPoint.getSubscriberId());
                    textAddress.setText("Address: " + selectedDropOffPoint.getAddress());
                    textPhone.setText("Phone: " + selectedDropOffPoint.getPhone());
                    textPaperTypes.setText("Paper Types: " + paperTypesToString(selectedDropOffPoint.getPaperTypes()));
                    textDeliveryStatus.setText("Delivery Status: " + selectedDropOffPoint.getDeliveryStatus());
                    textRenewalReminderIssued.setText("Is Renewal Reminder Required: " + selectedDropOffPoint.isRenewalReminderIssued());
                    textDeliveryHistory.setText("Delivery History: " + deliveryHistoryToString(selectedDropOffPoint.getDeliveryHistory()));

                    // Change marker color to green if delivery status is "Delivered"
                    if (selectedDropOffPoint.getDeliveryStatus().equalsIgnoreCase("Delivered")) {
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    }
                }
            }

            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return view;
    }





    private void showDropOffPointsOnMap() {
        if (googleMap != null) {
            googleMap.clear(); // Clear existing markers

            for (DropOffPoint dropOffPoint : dropOffPointsList) {
                double latitude = dropOffPoint.getLatitude();
                double longitude = dropOffPoint.getLongitude();
                String name = dropOffPoint.getName();
                String address = dropOffPoint.getAddress();
                String subscriberId = dropOffPoint.getSubscriberId();
                String phone = dropOffPoint.getPhone();
                List<String> paperTypes = dropOffPoint.getPaperTypes();
                String deliveryStatus = dropOffPoint.getDeliveryStatus();
                boolean renewalReminderIssued = dropOffPoint.isRenewalReminderIssued();
                List<String> deliveryHistory = dropOffPoint.getDeliveryHistory();
                LatLng location = new LatLng(latitude, longitude);

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(location)
                        .title(name)
                        .snippet("Subscriber ID: " + subscriberId + "\nAddress: " + address + "\nPhone: " + phone + "\nPaper Types: " + paperTypesToString(paperTypes) + "\nDelivery Status: " + deliveryStatus + "\nIs Renewal Reminder Required: " + renewalReminderIssued + "\nDelivery History: " + deliveryHistoryToString(deliveryHistory));

                markerOptions.infoWindowAnchor(0.5f, 0.5f);
                googleMap.addMarker(markerOptions);
            }

            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View view = getLayoutInflater().inflate(R.layout.marker_info_window, null);

                    TextView textName = view.findViewById(R.id.text_name);
                    TextView textSubscriberId = view.findViewById(R.id.text_subscriber_id);
                    TextView textAddress = view.findViewById(R.id.text_address);
                    TextView textPhone = view.findViewById(R.id.text_phone);
                    TextView textPaperTypes = view.findViewById(R.id.text_paper_types);
                    TextView textDeliveryStatus = view.findViewById(R.id.text_delivery_Status);
                    TextView textRenewalReminderIssued = view.findViewById(R.id.text_renewalReminderIssued);
                    TextView textDeliveryHistory = view.findViewById(R.id.text_deliveryHistory);

                    DropOffPoint selectedDropOffPoint = null;
                    if (marker.equals(currentLocationMarker)) {
                        textName.setText("Your Current Location");
                    } else {
                        DatabaseHelper databaseHelper = new DatabaseHelper(context);
                        SQLiteDatabase db = databaseHelper.getReadableDatabase();
                        String[] columns = {
                                DatabaseHelper.COLUMN_ID,
                                DatabaseHelper.COLUMN_NAME,
                                DatabaseHelper.COLUMN_ADDRESS,
                                DatabaseHelper.COLUMN_PHONE,
                                DatabaseHelper.COLUMN_PAPER_TYPES,
                                DatabaseHelper.COLUMN_DELIVERY_STATUS,
                                DatabaseHelper.COLUMN_RENEWAL_REMINDER_ISSUED,
                                DatabaseHelper.COLUMN_DELIVERY_HISTORY
                        };
                        String selection = DatabaseHelper.COLUMN_NAME + " = ?";
                        String[] selectionArgs = { marker.getTitle() };
                        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, columns, selection, selectionArgs, null, null, null);

                        if (cursor != null && cursor.moveToFirst()) {
                            int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
                            int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME);
                            int addressIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ADDRESS);
                            int phoneIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PHONE);
                            int paperTypesIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PAPER_TYPES);
                            int deliveryStatusIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DELIVERY_STATUS);
                            int renewalReminderIssuedIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_RENEWAL_REMINDER_ISSUED);
                            int deliveryHistoryIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DELIVERY_HISTORY);

                            if (idIndex >= 0 && nameIndex >= 0 && addressIndex >= 0 && phoneIndex >= 0 && paperTypesIndex >= 0 && deliveryStatusIndex >= 0 && renewalReminderIssuedIndex >= 0 && deliveryHistoryIndex >= 0) {
                                String id = cursor.getString(idIndex);
                                String name = cursor.getString(nameIndex);
                                String address = cursor.getString(addressIndex);
                                String phone = cursor.getString(phoneIndex);
                                String paperTypes = cursor.getString(paperTypesIndex);
                                String deliveryStatus = cursor.getString(deliveryStatusIndex);
                                int renewalReminderIssued = cursor.getInt(renewalReminderIssuedIndex);
                                String deliveryHistory = cursor.getString(deliveryHistoryIndex);

                                selectedDropOffPoint = new DropOffPoint(
                                        0.0, // Set the initial values for latitude and longitude, you may update them based on your data
                                        0.0,
                                        stringToPaperTypes(paperTypes),
                                        deliveryStatus,
                                        "",
                                        false,
                                        renewalReminderIssued == 1,
                                        "",
                                        id,
                                        name,
                                        address,
                                        phone,
                                        stringToDeliveryHistory(deliveryHistory)
                                );
                                textName.setText(marker.getTitle());
                                textSubscriberId.setText("Subscriber ID: " + selectedDropOffPoint.getSubscriberId());
                                textAddress.setText("Address: " + selectedDropOffPoint.getAddress());
                                textPhone.setText("Phone: " + selectedDropOffPoint.getPhone());
                                textPaperTypes.setText("Paper Types: " + paperTypesToString(selectedDropOffPoint.getPaperTypes()));
                                textDeliveryStatus.setText("Delivery Status: " + selectedDropOffPoint.getDeliveryStatus());
                                textRenewalReminderIssued.setText("Is Renewal Reminder Required: " + selectedDropOffPoint.isRenewalReminderIssued());
                                textDeliveryHistory.setText("Delivery History: " + deliveryHistoryToString(selectedDropOffPoint.getDeliveryHistory()));

                                // Change marker color to green if delivery status is "Delivered"
                                if (selectedDropOffPoint.getDeliveryStatus().equalsIgnoreCase("Delivered")) {
                                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                }
                            }
                        }

                        if (cursor != null) {
                            cursor.close();
                        }
                        db.close();
                    }

                    return view;
                }
            });

            if (!dropOffPointsList.isEmpty()) {
                DropOffPoint firstDropOffPoint = dropOffPointsList.get(0);
                double latitude = firstDropOffPoint.getLatitude();
                double longitude = firstDropOffPoint.getLongitude();
                LatLng firstLocation = new LatLng(latitude, longitude);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 12));
            }
        }
    }

    private String paperTypesToString(List<String> paperTypes) {
        StringBuilder sb = new StringBuilder();
        for (String type : paperTypes) {
            sb.append(type).append(", ");
        }
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }

    private String deliveryHistoryToString(List<String> deliveryHistory) {
        StringBuilder sb = new StringBuilder();
        for (String history : deliveryHistory) {
            sb.append(history).append(", ");
        }
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }
    // Utility method to convert a comma-separated string to a list of paper types
    private List<String> stringToPaperTypes(String paperTypesString) {
        List<String> paperTypes = new ArrayList<>();
        if (paperTypesString != null && !paperTypesString.isEmpty()) {
            String[] paperTypesArray = paperTypesString.split(",");
            for (String paperType : paperTypesArray) {
                paperTypes.add(paperType.trim());
            }
        }
        return paperTypes;
    }

    // Utility method to convert a comma-separated string to a list of delivery history
    private List<String> stringToDeliveryHistory(String deliveryHistoryString) {
        List<String> deliveryHistory = new ArrayList<>();
        if (deliveryHistoryString != null && !deliveryHistoryString.isEmpty()) {
            String[] deliveryHistoryArray = deliveryHistoryString.split(",");
            for (String history : deliveryHistoryArray) {
                deliveryHistory.add(history.trim());
            }
        }
        return deliveryHistory;
    }

    private void retrieveDropOffPoints() {
        dropOffPointsList.clear();

        String json = loadJSONFromAsset("delivery_plan.json");

        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String subscriberId = jsonObject.getString("subscriberId");
                String name = jsonObject.getString("name");
                String address = jsonObject.getString("address");
                String phone = jsonObject.getString("phone");
                JSONArray paperTypesArray = jsonObject.getJSONArray("paperTypes");
                List<String> paperTypes = new ArrayList<>();
                for (int j = 0; j < paperTypesArray.length(); j++) {
                    paperTypes.add(paperTypesArray.getString(j));
                }
                JSONObject dropOffLocationObject = jsonObject.getJSONObject("dropOffLocation");
                double latitude = dropOffLocationObject.getDouble("latitude");
                double longitude = dropOffLocationObject.getDouble("longitude");

                String deliveryStatus = jsonObject.getString("deliveryStatus");
                String deliveryTimestamp = jsonObject.getString("deliveryTimestamp");
                boolean ShouldRenewalReminderIssued = jsonObject.getBoolean("ShouldRenewalReminderIssued");
                boolean isRenewalReminderIssued = jsonObject.getBoolean("isRenewalReminderIssued");
                JSONArray deliveryHistoryArray = jsonObject.getJSONArray("deliveryHistory");
                List<String> deliveryHistory = new ArrayList<>();
                for (int j = 0; j < deliveryHistoryArray.length(); j++) {
                    deliveryHistory.add(deliveryHistoryArray.getString(j));
                }
                String note = jsonObject.getString("note");
                DropOffPoint dropOffPoint = new DropOffPoint(latitude, longitude, paperTypes, deliveryStatus, deliveryTimestamp, ShouldRenewalReminderIssued, isRenewalReminderIssued, note, subscriberId, name, address, phone, deliveryHistory);
                dropOffPointsList.add(dropOffPoint);

                DatabaseHelper databaseHelper = new DatabaseHelper(context);
                databaseHelper.insertDropOffPoints(dropOffPointsList);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableLocationLayer();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }
    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    // Implement the onLocationChanged method of the LocationListener interface
    @Override
    public void onLocationChanged(Location location) {
        // Handle the new location update
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        // Update the current location marker
        LatLng currentLocation = new LatLng(latitude, longitude);
        if (currentLocationMarker != null) {
            currentLocationMarker.setPosition(currentLocation);
        } else {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(currentLocation)
                    .title("Your Current Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

            currentLocationMarker = googleMap.addMarker(markerOptions);
        }

        // Do something with the new location coordinates
    }



    private void enableLocationLayer() {
        if (googleMap != null) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    private String loadJSONFromAsset(String fileName) {
        String json;
        try {
            InputStream inputStream = getAssets().open(fileName);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocationLayer();
            }
        }
    }


    public boolean onMarkerClick(Marker marker) {
        if (marker.equals(selectedMarker)) {
            deliveryStatusButton.setEnabled(true);
            selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        } else {
            deliveryStatusButton.setEnabled(false);
        }
        return false;
    }

    private void showUpdateDeliveryStatusWindow() {
        // Create a dialog to show the update delivery status window
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.update_delivery_status_window);

        // Get relevant views from the update_delivery_status_window layout
        Spinner spinnerDeliveryStatus = dialog.findViewById(R.id.spinner_deliveryStatus);
        Spinner spinnerRenewalReminder = dialog.findViewById(R.id.spinner_renewalReminder);
        Button updateButton = dialog.findViewById(R.id.button_update);
        EditText noteEditText = dialog.findViewById(R.id.editText_note);

        // Set up the adapter for the Delivery Status spinner
        ArrayAdapter<CharSequence> deliveryStatusAdapter = ArrayAdapter.createFromResource(this,
                R.array.delivery_status_options, android.R.layout.simple_spinner_item);
        deliveryStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDeliveryStatus.setAdapter(deliveryStatusAdapter);

        // Set up the adapter for the Renewal Reminder spinner
        ArrayAdapter<CharSequence> renewalReminderAdapter = ArrayAdapter.createFromResource(this,
                R.array.renewal_reminder_options, android.R.layout.simple_spinner_item);
        renewalReminderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRenewalReminder.setAdapter(renewalReminderAdapter);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the updated values from the input fields
                String deliveryStatus = spinnerDeliveryStatus.getSelectedItem().toString();
                String note = noteEditText.getText().toString();
                String deliveryTimestamp = getCurrentDateTime();
                boolean isRenewalReminderIssued = Boolean.parseBoolean(spinnerRenewalReminder.getSelectedItem().toString());

                String subscriberId = getSubscriberIdFromMarker(selectedMarker);


                boolean updateSuccessful = updateSubscriberDeliveryStatus(subscriberId, deliveryStatus, note, deliveryTimestamp, isRenewalReminderIssued);

                if (updateSuccessful) {
                    // Update the marker details on the map
                    updateMarkerDeliveryStatus(selectedMarker, deliveryStatus);

                    // Set the marker icon color to green
                    selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

                    // Show a toast message indicating successful update
                    Toast.makeText(TripActivity.this, "Delivery update successful", Toast.LENGTH_SHORT).show();
                } else {
                    // Show a toast message indicating failed update
                    Toast.makeText(TripActivity.this, "Failed to update delivery", Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();
            }
        });

        dialog.show();
    }




    public String getSubscriberIdFromMarker(Marker marker) {
        if (marker.equals(currentLocationMarker)) {
            return ""; // Return an empty string for the current location marker
        } else {
            DatabaseHelper databaseHelper = new DatabaseHelper(this);
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            String[] columns = { DatabaseHelper.COLUMN_ID };
            String selection = DatabaseHelper.COLUMN_NAME + " = ?";
            String[] selectionArgs = { marker.getTitle() };
            Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, columns, selection, selectionArgs, null, null, null);

            String subscriberId = "";
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
                if (idIndex >= 0) {
                    subscriberId = cursor.getString(idIndex);
                }
            }

            if (cursor != null) {
                cursor.close();
            }
            db.close();

            return subscriberId;
        }
    }

    private String getCurrentDateTime() {
        // Return the current date and time in the desired format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(new Date());
    }


    private boolean updateSubscriberDeliveryStatus(String subscriberId, String deliveryStatus, String note, String deliveryTimestamp, boolean isRenewalReminderIssued) {
        // Update the subscriber's delivery status in the database using DatabaseHelper
        // Assuming you have a DatabaseHelper class with relevant methods for updating the subscribers table

        // Instantiate your DatabaseHelper class
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        // Assuming you have a method in DatabaseHelper to update the subscriber's delivery status
        return dbHelper.updateSubscriberDeliveryStatus(subscriberId, deliveryStatus, note, deliveryTimestamp, isRenewalReminderIssued);
    }

    private void updateMarkerDeliveryStatus(Marker marker, String deliveryStatus) {
        // Update the delivery status of the selected marker
        // Assuming you have stored the delivery status as the marker's snippet
        marker.setSnippet(deliveryStatus);
    }



    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }



    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}




}

