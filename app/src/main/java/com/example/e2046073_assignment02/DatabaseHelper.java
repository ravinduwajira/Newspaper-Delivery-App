package com.example.e2046073_assignment02;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;

import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "delivery.sqlite";
    private static final int DATABASE_VERSION = 1;

    // Distributors table
    public static final String TABLE_DISTRIBUTORS = "distributors";
    public static final String COLUMN_DISTRIBUTOR_ID = "distributor_id";
    public static final String COLUMN_PASSWORD = "password";

    public static final String TABLE_NAME = "subscribers";
    public static final String COLUMN_ID = "subscriberId";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_PAPER_TYPES = "paper_types";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_DELIVERY_STATUS = "delivery_status";
    public static final String COLUMN_DELIVERY_TIMESTAMP = "delivery_timestamp";
    public static final String COLUMN_RENEWAL_REMINDER_SHOULD_ISSUE = "renewal_reminder_should_issue";
    public static final String COLUMN_RENEWAL_REMINDER_ISSUED = "renewal_reminder_issued";
    public static final String COLUMN_NOTE = "note";
    public static final String COLUMN_DELIVERY_HISTORY = "delivery_history";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the distributors table
        String createDistributorsTableQuery = "CREATE TABLE " + TABLE_DISTRIBUTORS + "(" +
                COLUMN_DISTRIBUTOR_ID + " TEXT PRIMARY KEY," +
                COLUMN_PASSWORD + " TEXT)";
        db.execSQL(createDistributorsTableQuery);

        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_ADDRESS + " TEXT, " +
                COLUMN_PHONE + " TEXT, " +
                COLUMN_PAPER_TYPES + " TEXT, " +
                COLUMN_LATITUDE + " DOUBLE, " +
                COLUMN_LONGITUDE + " DOUBLE, " +
                COLUMN_DELIVERY_STATUS + " TEXT, " +
                COLUMN_DELIVERY_TIMESTAMP + " TEXT, " +
                COLUMN_RENEWAL_REMINDER_SHOULD_ISSUE + " BOOLEAN, " +
                COLUMN_RENEWAL_REMINDER_ISSUED + " BOOLEAN, " +
                COLUMN_NOTE + " TEXT, " +
                COLUMN_DELIVERY_HISTORY + " TEXT" +
                ")";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrade if needed
        // This method will be called when DATABASE_VERSION is incremented
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DISTRIBUTORS);
    }

    // Distributor-related operations

    public boolean insertDistributor(String distributorId, String password) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_DISTRIBUTOR_ID, distributorId);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_DISTRIBUTORS, null, values);
        return result != -1;
    }

    public void insertDropOffPoints(List<DropOffPoint> dropOffPoints) {
        SQLiteDatabase db = getWritableDatabase();

        for (DropOffPoint dropOffPoint : dropOffPoints) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, dropOffPoint.getSubscriberId());
            values.put(COLUMN_NAME, dropOffPoint.getName());
            values.put(COLUMN_ADDRESS, dropOffPoint.getAddress());
            values.put(COLUMN_PHONE, dropOffPoint.getPhone());

            JSONArray paperTypesArray = new JSONArray(dropOffPoint.getPaperTypes());
            values.put(COLUMN_PAPER_TYPES, paperTypesArray.toString());

            values.put(COLUMN_LATITUDE, dropOffPoint.getLatitude());
            values.put(COLUMN_LONGITUDE, dropOffPoint.getLongitude());
            values.put(COLUMN_DELIVERY_STATUS, dropOffPoint.getDeliveryStatus());
            values.put(COLUMN_DELIVERY_TIMESTAMP, dropOffPoint.getDeliveryTimestamp());
            values.put(COLUMN_RENEWAL_REMINDER_SHOULD_ISSUE, dropOffPoint.getShouldRenewalReminderIssued());
            values.put(COLUMN_RENEWAL_REMINDER_ISSUED, dropOffPoint.isRenewalReminderIssued());

            JSONArray deliveryHistoryArray = new JSONArray(dropOffPoint.getDeliveryHistory());
            values.put(COLUMN_DELIVERY_HISTORY, deliveryHistoryArray.toString());

            values.put(COLUMN_NOTE, dropOffPoint.getNote());

            db.insert(TABLE_NAME, null, values);
        }
    }
    public boolean updateSubscriberDeliveryStatus(String subscriberId, String deliveryStatus, String note, String deliveryTimestamp, boolean isRenewalReminderIssued) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DELIVERY_STATUS, deliveryStatus);
        values.put(COLUMN_NOTE, note);
        values.put(COLUMN_DELIVERY_TIMESTAMP, deliveryTimestamp);
        values.put(COLUMN_RENEWAL_REMINDER_ISSUED, isRenewalReminderIssued ? 1 : 0);

        String whereClause = COLUMN_ID + "=?";
        String[] whereArgs = new String[]{subscriberId};

        int rowsAffected = db.update(TABLE_NAME, values, whereClause, whereArgs);
        db.close();

        return rowsAffected > 0;
    }



}
