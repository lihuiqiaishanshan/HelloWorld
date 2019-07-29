package com.li.demo.contentprovider;

import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;

public class ProviderContacts implements BaseColumns {
        public ProviderContacts(){
        }
        public static final String AUTHORITY = 
                "konka.contentproviders.contacts";
        public static final String TB_NAME = "weatherinfo";
        public static final Uri CONTENT_URI = Uri.parse(
                "content://" + AUTHORITY + "/" + TB_NAME);
        
        public static final int CONTACTS = 1;
        public static final int CONTACT_ID = 2;
        public static final UriMatcher uriMatcher;
        static{
                uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
                uriMatcher.addURI(AUTHORITY,"weatherinfo",CONTACTS);
                uriMatcher.addURI(AUTHORITY,"weatherinfo/#",CONTACT_ID);
        }
        
        public static final String _ID = "_id";
        public static final String CITY = "city";
        public static final String WEATHER = "weather";
        public static final String LOWTEMP = "low_temp";
        public static final String HIGHTEMP = "high_temp";
        public static final String DATE = "date";
}
