package com.li.demo.ui;

import com.li.demo.R;
import com.li.demo.contentprovider.MyContacts;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class CPActivity extends Activity {
        private Button btInsertData = null;
        private Button btViewData = null;
        private Button btDelOne = null;
        private Button btClearAll = null;
        private Button btUpdateAll = null;

        /** Called when the activity is first created. */
        @Override
        public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.main);

                btInsertData = (Button) findViewById(R.id.Button02);
                btInsertData.setOnClickListener(new ClickViewHandler());
                btViewData = (Button) findViewById(R.id.Button03);
                btViewData.setOnClickListener(new ClickViewHandler());
                btDelOne = (Button) findViewById(R.id.Button04);
                btDelOne.setOnClickListener(new ClickViewHandler());
                btClearAll = (Button) findViewById(R.id.Button05);
                btClearAll.setOnClickListener(new ClickViewHandler());
                btUpdateAll = (Button) findViewById(R.id.Button06);
                btUpdateAll.setOnClickListener(new ClickViewHandler());
        }

        public class ClickViewHandler implements OnClickListener {
                @Override
                public void onClick(View v) {
                        if (v == btInsertData) {
                                InsertSomeRecords();
                        } else if (v == btViewData) {
                                ViewRecords();
                        } else if (v == btDelOne) {
                                DelOne();
                        } else if (v == btClearAll) {
                                DelAllPeople();
                        }else if(v == btUpdateAll){
                        	updateAll();
                        }
                }

                private void DelAllPeople() {
                        getContentResolver().delete(MyContacts.CONTENT_URI, null, null);
                }
                
                private void updateAll(){
                    ContentValues values = new ContentValues();
                    values.put(MyContacts.NAME, "李慧奇");
                    values.put(MyContacts.NUMBER1, "18503050023");
                    values.clear();
                    values.put(MyContacts.NAME, "战三");
                    values.put(MyContacts.NUMBER1, "33333333333");
                	getContentResolver().update(MyContacts.CONTENT_URI, values, null, null);
                }

                private void DelOne() {
                        int id;
                        Cursor c = getContentResolver().query(
                                        MyContacts.CONTENT_URI, null, 
                                        null, null, MyContacts.NAME + " ASC");
                        if (c.moveToFirst()) {
                                int idxID = c.getColumnIndex(MyContacts._ID);
                                id = c.getInt(idxID);
                                getContentResolver().delete(MyContacts.CONTENT_URI, 
                                                MyContacts._ID + " = " + id, null);
                        }
                }

                private void ViewRecords() {
                        // Make the query
                        Cursor c = managedQuery(MyContacts.CONTENT_URI, null, null, null,
                                        MyContacts._ID);
                        StringBuilder sbRecords = new StringBuilder("");
                        if (c.moveToFirst()) {
                                int idxID = c.getColumnIndex(MyContacts._ID);
                                int idxName = c.getColumnIndex(MyContacts.NAME);
                                int idxNumber = c.getColumnIndex(MyContacts.NUMBER1);
                                int idxEmail = c.getColumnIndex(MyContacts.EMAIL);
                                // Iterator the records
                                do {
                                        sbRecords.append(c.getInt(idxID));
                                        sbRecords.append(". ");
                                        sbRecords.append(c.getString(idxName));
                                        sbRecords.append(", ");
                                        sbRecords.append(c.getString(idxNumber));
                                        sbRecords.append(", ");
                                        sbRecords.append(c.getString(idxEmail));
                                        sbRecords.append("/n");
                                } while (c.moveToNext());
                        }
                        c.close();
                        // Refresh the content of TextView
                        ((TextView)(findViewById(
                                        R.id.TextView01))).setText(sbRecords);
                }

                private void InsertSomeRecords() {
                        ContentValues value = new ContentValues();
                        value.put(MyContacts.NAME, "朱元璋");
                        value.put(MyContacts.NUMBER1, "13965625585");
                        ContentValues value1 = new ContentValues();
                        value1.put(MyContacts.NAME, "玄烨");
                        value1.put(MyContacts.EMAIL, "xueye1772@gmail.com");
                        ContentValues [] values = new ContentValues[2];
                        values[0] = value;
                        values[1]= value1;
                        getContentResolver().bulkInsert(MyContacts.CONTENT_URI, values);
                }
        }
}