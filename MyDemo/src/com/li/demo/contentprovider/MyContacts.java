package com.li.demo.contentprovider;

import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;

public class MyContacts implements BaseColumns {
        public MyContacts(){
        }
        public static final String AUTHORITY = 
                "jtapp.contentproviders.contacts";
        public static final String TB_NAME = "mycontacts";
        public static final Uri CONTENT_URI = Uri.parse(
                "content://" + AUTHORITY + "/" + TB_NAME);
        
        public static final int CONTACTS = 1;
        public static final int CONTACT_ID = 2;
        public static final UriMatcher uriMatcher;
        static{
                uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
                uriMatcher.addURI(AUTHORITY,"mycontacts",CONTACTS);
                uriMatcher.addURI(AUTHORITY,"mycontacts/#",CONTACT_ID);
        }
        
        public static final String _ID = "id";
        public static final String NAME = "name";
        public static final String NUMBER1 = "number1";
        public static final String EMAIL = "email";
}
