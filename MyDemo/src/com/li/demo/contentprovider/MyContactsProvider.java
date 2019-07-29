package com.li.demo.contentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class MyContactsProvider extends ContentProvider {
        private static final String TAG = "MyContactsProvider"; 
        private static SQLiteDatabase mDB;

        private static void createTablesIfNotExists() {
                mDB.execSQL("CREATE TABLE IF NOT EXISTS "    
                + MyContacts.TB_NAME + " ("    
                + MyContacts._ID + " INTEGER PRIMARY KEY,"    
                + MyContacts.NAME + " VARCHAR,"  
                + MyContacts.NUMBER1 + " VARCHAR,"
                + MyContacts.EMAIL + " VARCHAR)");
        }

        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs) {
                int count;
                switch (MyContacts.uriMatcher.match(uri)) {
                        case MyContacts.CONTACTS:
                                count = mDB.delete(MyContacts.TB_NAME, 
                                                selection, selectionArgs);
                                break;
                        case MyContacts.CONTACT_ID:
                                String contactID = uri.getPathSegments().get(1);
                                count = mDB.delete(MyContacts.TB_NAME, 
                                                MyContacts._ID + "=" + contactID, selectionArgs);
                                break;
                        default: throw new IllegalArgumentException(
                                        "Unsupported URI: " + uri);
                }
                return count;
        }

        @Override
        public String getType(Uri uri) {
                switch (MyContacts.uriMatcher.match(uri)) {
                        case MyContacts.CONTACTS:
                        return "vnd.android.cursor.dir/vnd.jtapp.contacts";
                        case MyContacts.CONTACT_ID:
                        return "vnd.android.cursor.item/vnd.ambow.contacts";
                        default:
                        throw new IllegalArgumentException("Unsupported URI: " + uri);
                }
        }

        @Override
        public Uri insert(Uri uri, ContentValues contentValues) {
                long rowId = mDB.insert(MyContacts.TB_NAME, null, contentValues);
                if (rowId > 0) {
                        Uri noteUri = 
                                ContentUris.withAppendedId(MyContacts.CONTENT_URI,rowId);
                        getContext().getContentResolver().notifyChange(noteUri, null);
                        Log.d(TAG+"insert",noteUri.toString());
                        return noteUri;
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
        }

        @Override
        public boolean onCreate() {
                if (mDB == null) {
                        mDB = this.getContext().openOrCreateDatabase(MyContacts.TB_NAME,
                                        Context.MODE_PRIVATE, null);
                        createTablesIfNotExists();
                }
                return mDB != null;
        }

        @Override
        public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
                SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
                qb.setTables(MyContacts.TB_NAME);
                
                switch (MyContacts.uriMatcher.match(uri)) {
                        case MyContacts.CONTACT_ID:
                                qb.appendWhere(
                                                MyContacts._ID + "=" + uri.getPathSegments().get(1));
                                break;
                }
                String orderBy;
                if (TextUtils.isEmpty(sortOrder)) {
                        orderBy = MyContacts._ID;
                } else {
                        orderBy = sortOrder;
                }
                Cursor c = qb.query(mDB, projection, selection, 
                                selectionArgs,null, null,orderBy);
                return c;        
        }

        @Override
        public int update(Uri uri, ContentValues contentValues, String selection,
                        String[] selectionArgs) {

                Log.d(TAG+"update",contentValues.toString());
                Log.d(TAG+"update",uri.toString());
                
                int count;                
                switch (MyContacts.uriMatcher.match(uri)) {
                        case MyContacts.CONTACTS:
                                Log.d(TAG+"update",MyContacts.CONTACTS+"");
                                count = mDB.update(
                                                MyContacts.TB_NAME, contentValues, 
                                                selection, selectionArgs);
                                break;
                        case MyContacts.CONTACT_ID:
                                String contactID = uri.getPathSegments().get(1);
                                Log.d(TAG+"update",contactID+"");
                                count = mDB.update(MyContacts.TB_NAME,contentValues,
                                                MyContacts._ID + "=" + contactID, 
                                                selectionArgs);
                                break;
                        default: throw new IllegalArgumentException(
                                        "Unsupported URI: " + uri);
                }
                return count;
        }
}
