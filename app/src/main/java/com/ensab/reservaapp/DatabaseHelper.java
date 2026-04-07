package com.ensab.reservaapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "HRBooking.db";
    private static final int DATABASE_VERSION = 4; // Version incremented for profile image

    // ─── Table Users ───────────────────────────────────────────
    private static final String TABLE_USERS        = "users";
    private static final String COL_USER_ID        = "id";
    private static final String COL_FULL_NAME      = "full_name";
    private static final String COL_EMAIL          = "email";
    private static final String COL_PHONE          = "phone";
    private static final String COL_PASSWORD       = "password";
    private static final String COL_PROFILE_IMAGE  = "profile_image"; // Added

    // ─── Table Hotels ──────────────────────────────────────────
    private static final String TABLE_HOTELS       = "hotels";
    private static final String COL_HOTEL_ID       = "id";
    private static final String COL_HOTEL_NAME     = "name";
    private static final String COL_HOTEL_CITY     = "city";
    private static final String COL_HOTEL_DESC     = "description";
    private static final String COL_HOTEL_PRICE    = "price_per_night";
    private static final String COL_HOTEL_RATING   = "rating";

    // ─── Table Restaurants ─────────────────────────────────────
    private static final String TABLE_RESTAURANTS  = "restaurants";
    private static final String COL_RESTO_ID       = "id";
    private static final String COL_RESTO_NAME     = "name";
    private static final String COL_RESTO_CITY     = "city";
    private static final String COL_RESTO_CUISINE  = "cuisine";
    private static final String COL_RESTO_PRICE    = "price_range";
    private static final String COL_RESTO_RATING   = "rating";

    // ─── Table Reservations ────────────────────────────────────
    private static final String TABLE_RESERVATIONS = "reservations";
    private static final String COL_RES_ID         = "id";
    private static final String COL_RES_USER_ID    = "user_id";
    private static final String COL_RES_TYPE       = "type";      // 'hotel' ou 'restaurant'
    private static final String COL_RES_REF_ID     = "ref_id";    // id hotel ou restaurant
    private static final String COL_RES_DATE       = "date";
    private static final String COL_RES_GUESTS     = "guests";
    private static final String COL_RES_STATUS     = "status";    // 'confirmed' ou 'cancelled'

    // ─── Table Saved (Favoris) ─────────────────────────────────
    private static final String TABLE_SAVED        = "saved";
    private static final String COL_SAVED_ID       = "id";
    private static final String COL_SAVED_USER_ID  = "user_id";
    private static final String COL_SAVED_TYPE     = "type";
    private static final String COL_SAVED_REF_ID   = "ref_id";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
        insertSampleData(db);
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USERS + "("
                + COL_USER_ID   + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_FULL_NAME + " TEXT,"
                + COL_EMAIL     + " TEXT UNIQUE,"
                + COL_PHONE     + " TEXT,"
                + COL_PASSWORD  + " TEXT,"
                + COL_PROFILE_IMAGE + " TEXT)"); // Added

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_HOTELS + "("
                + COL_HOTEL_ID     + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_HOTEL_NAME   + " TEXT,"
                + COL_HOTEL_CITY   + " TEXT,"
                + COL_HOTEL_DESC   + " TEXT,"
                + COL_HOTEL_PRICE  + " REAL,"
                + COL_HOTEL_RATING + " REAL)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_RESTAURANTS + "("
                + COL_RESTO_ID      + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_RESTO_NAME    + " TEXT,"
                + COL_RESTO_CITY    + " TEXT,"
                + COL_RESTO_CUISINE + " TEXT,"
                + COL_RESTO_PRICE   + " TEXT,"
                + COL_RESTO_RATING  + " REAL)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_RESERVATIONS + "("
                + COL_RES_ID      + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_RES_USER_ID + " INTEGER,"
                + COL_RES_TYPE    + " TEXT,"
                + COL_RES_REF_ID  + " INTEGER,"
                + COL_RES_DATE    + " TEXT,"
                + COL_RES_GUESTS  + " INTEGER,"
                + COL_RES_STATUS  + " TEXT,"
                + "FOREIGN KEY(" + COL_RES_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "))");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SAVED + "("
                + COL_SAVED_ID      + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_SAVED_USER_ID + " INTEGER,"
                + COL_SAVED_TYPE    + " TEXT,"
                + COL_SAVED_REF_ID  + " INTEGER,"
                + "FOREIGN KEY(" + COL_SAVED_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "))");
    }

    private void insertSampleData(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + TABLE_HOTELS + " (name, city, description, price_per_night, rating) VALUES ('Royal Mansour', 'Marrakech', 'Palatial hotel with riads and luxury spas.', 1200, 4.9)");
        db.execSQL("INSERT INTO " + TABLE_HOTELS + " (name, city, description, price_per_night, rating) VALUES ('Sofitel Casablanca', 'Casablanca', 'Modern art-deco style with panoramic views.', 250, 4.5)");
        
        db.execSQL("INSERT INTO " + TABLE_RESTAURANTS + " (name, city, cuisine, price_range, rating) VALUES ('Le Grand Table Marocaine', 'Marrakech', 'Moroccan Haute Cuisine', '$$$$', 4.8)");
        db.execSQL("INSERT INTO " + TABLE_RESTAURANTS + " (name, city, cuisine, price_range, rating) VALUES ('Rick''s Café', 'Casablanca', 'International/Seafood', '$$$', 4.6)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            createTables(db);
        }
        if (oldVersion < 3) {
            try { db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_PHONE + " TEXT"); } catch (Exception ignored) {}
        }
        if (oldVersion < 4) {
            try { db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_PROFILE_IMAGE + " TEXT"); } catch (Exception ignored) {}
        }
    }

    public boolean addUser(String fullName, String email, String phone, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FULL_NAME, fullName);
        values.put(COL_EMAIL, email);
        values.put(COL_PHONE, phone);
        values.put(COL_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID}, COL_EMAIL + "=? AND " + COL_PASSWORD + "=?", new String[]{email, password}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID}, COL_EMAIL + "=?", new String[]{email}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PASSWORD, newPassword);
        int result = db.update(TABLE_USERS, values, COL_EMAIL + "=?", new String[]{email});
        db.close();
        return result > 0;
    }

    public boolean updateFullName(String email, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FULL_NAME, newName);
        int result = db.update(TABLE_USERS, values, COL_EMAIL + "=?", new String[]{email});
        db.close();
        return result > 0;
    }

    public boolean updatePhone(String email, String newPhone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PHONE, newPhone);
        int result = db.update(TABLE_USERS, values, COL_EMAIL + "=?", new String[]{email});
        db.close();
        return result > 0;
    }

    public boolean updateProfileImage(String email, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PROFILE_IMAGE, imageUri);
        int result = db.update(TABLE_USERS, values, COL_EMAIL + "=?", new String[]{email});
        db.close();
        return result > 0;
    }

    public String getUserFullName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_FULL_NAME}, COL_EMAIL + "=?", new String[]{email}, null, null, null);
        String name = "";
        if (cursor.moveToFirst()) name = cursor.getString(0);
        cursor.close();
        db.close();
        return name;
    }

    public String getUserPhone(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_PHONE}, COL_EMAIL + "=?", new String[]{email}, null, null, null);
        String phone = "";
        if (cursor.moveToFirst()) phone = cursor.getString(0);
        cursor.close();
        db.close();
        return phone;
    }

    public String getUserProfileImage(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_PROFILE_IMAGE}, COL_EMAIL + "=?", new String[]{email}, null, null, null);
        String image = null;
        if (cursor.moveToFirst()) image = cursor.getString(0);
        cursor.close();
        db.close();
        return image;
    }

    public List<String[]> getAllHotels() {
        List<String[]> hotels = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_HOTELS, null);
        if (cursor.moveToFirst()) {
            do {
                hotels.add(new String[]{cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5)});
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return hotels;
    }

    public List<String[]> getAllRestaurants() {
        List<String[]> restaurants = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_RESTAURANTS, null);
        if (cursor.moveToFirst()) {
            do {
                restaurants.add(new String[]{cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5)});
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return restaurants;
    }
}
