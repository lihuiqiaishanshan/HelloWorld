package com.li.demo.database;

import iapp.eric.utils.base.Trace;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {

	// 定义将数据库保存的位�?
	private String DB_PATH = "";

	private static String DB_NAME = "city.db";

	private static final String table = "nmc_iweather_city";

	private SQLiteDatabase myDataBase;

	private final Context myContext;

	/**
	 * Constructor Takes and keeps a reference of the passed context in order to
	 * access to the application assets and resources.
	 * 
	 * @param context
	 */
	public DataBaseHelper(Context context) {

		super(context, DB_NAME, null, 1);
		this.myContext = context;
		this.DB_PATH = context.getDatabasePath(DB_NAME).getAbsolutePath();
	}

	/**
	 * Creates a empty database on the system and rewrites it with your own
	 * database.
	 * */
	public void createDataBase() throws IOException {
		Trace.Info("createDataBase");

		// boolean dbExist = checkDataBase();
		//
		// if (dbExist) {
		// // do nothing - database already exist
		// } else {

		// 获取�?��可读的数据库
		//this.getReadableDatabase();

		try {

			copyDataBase();
			// 关闭
			//close();
		} catch (IOException e) {

			throw new Error("Error copying database");

		}
		// }

	}

	/**
	 * Check if the database already exist to avoid re-copying the file each
	 * time you open the application.
	 * 
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase() {

		SQLiteDatabase checkDB = null;

		try {
			String myPath = DB_PATH;
			checkDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READONLY);

		} catch (SQLiteCantOpenDatabaseException e) {
			e.printStackTrace();
		} catch (SQLiteException e) {
			e.printStackTrace();
		}

		if (checkDB != null) {

			boolean isTableExits = tabIsExist(table);
			if (isTableExits) {
				checkDB.close();
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}

	}

	/**
	 * Copies your database from your local assets-folder to the just created
	 * empty database in the system folder, from where it can be accessed and
	 * handled. This is done by transfering bytestream.
	 * */
	private void copyDataBase() throws IOException {
		Trace.Info("进入数据库拷贝方方法");
		// Open your local db as the input stream
		InputStream myInput = myContext.getAssets().open(DB_NAME);

		// Path to the just created empty db
		String outFileName = DB_PATH;

		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}

		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();

	}

	public void openDataBase() throws SQLException {

		// Open the database
		String myPath = DB_PATH;
		myDataBase = SQLiteDatabase.openDatabase(myPath, null,
				SQLiteDatabase.OPEN_READONLY);

	}

	@Override
	public synchronized void close() {

		if (myDataBase != null)
			myDataBase.close();

		super.close();

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		System.out.println("sqlitehelper oncreate");
		try {
			createDataBase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	// Add your public helper methods to access and get content from the
	// database.
	// You could return cursors by doing "return myDataBase.query(....)" so it'd
	// be easy
	// to you to create adapters for your views.

	/**
	 * 判断某张表是否存�?
	 * 
	 * @param tabName
	 *            表名
	 * @return
	 */
	public boolean tabIsExist(String tabName) {
		boolean result = false;
		if (tabName == null) {
			return false;
		}
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = this.getReadableDatabase();
			String sql = "select count(*) as c from sqlite_master where type ='table' and name ='"
					+ tabName.trim() + "' ";
			cursor = db.rawQuery(sql, null);
			if (cursor.moveToNext()) {
				int count = cursor.getInt(0);
				if (count > 0) {
					result = true;
				}
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
		return result;
	}

}
