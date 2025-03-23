package com.example.e2046073_assignment02;

import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DeliveryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);

        displayDeliveryPlan();
    }

    private void displayDeliveryPlan() {
        try {
            // Read the delivery plan data from the JSON file in the assets folder
            InputStream inputStream = getAssets().open("delivery_plan.json");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            // Parse the delivery plan data
            JSONArray deliveryPlanArray = new JSONArray(stringBuilder.toString());

            // Close the input streams
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();

            // Get the table layout from the XML
            TableLayout tableLayout = findViewById(R.id.tableLayout);

            // Loop through each data item and create a new row
            for (int i = 0; i < deliveryPlanArray.length(); i++) {
                JSONObject deliveryItem = deliveryPlanArray.getJSONObject(i);

                // Get the values from the JSON object
                String subscriberId = deliveryItem.getString("subscriberId");
                String name = deliveryItem.getString("name");
                String address = deliveryItem.getString("address");
                String phone = deliveryItem.getString("phone");

                // Create a new table row
                TableRow tableRow = new TableRow(this);

                // Create and add text views for each attribute
                TextView subscriberIdTextView = createTextView(subscriberId);
                TextView nameTextView = createTextView(name);
                TextView addressTextView = createTextView(address);
                TextView phoneTextView = createTextView(phone);

                tableRow.addView(subscriberIdTextView);
                tableRow.addView(nameTextView);
                tableRow.addView(addressTextView);
                tableRow.addView(phoneTextView);

                // Add the table row to the table layout
                tableLayout.addView(tableRow);
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            // Show an error message if there was an error reading or parsing the delivery plan file
            Toast.makeText(this, "Failed to read delivery plan", Toast.LENGTH_SHORT).show();
        }
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        textView.setText(text);
        return textView;

    }
}