/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\sync\\MyDemo\\src\\com\\konka\\multimedia\\modules\\music\\IRemote.aidl
 */
package com.konka.multimedia.modules.music;
public interface IRemote extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.konka.multimedia.modules.music.IRemote
{
private static final java.lang.String DESCRIPTOR = "com.konka.multimedia.modules.music.IRemote";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.konka.multimedia.modules.music.IRemote interface,
 * generating a proxy if needed.
 */
public static com.konka.multimedia.modules.music.IRemote asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.konka.multimedia.modules.music.IRemote))) {
return ((com.konka.multimedia.modules.music.IRemote)iin);
}
return new com.konka.multimedia.modules.music.IRemote.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_getPlayerTime:
{
data.enforceInterface(DESCRIPTOR);
long _result = this.getPlayerTime();
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_getTitle:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getTitle();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getSinger:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getSinger();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getDulcation:
{
data.enforceInterface(DESCRIPTOR);
long _result = this.getDulcation();
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_isPause:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isPause();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_isStart:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isStart();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getBit:
{
data.enforceInterface(DESCRIPTOR);
android.graphics.Bitmap _result = this.getBit();
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.konka.multimedia.modules.music.IRemote
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
//当前时间

@Override public long getPlayerTime() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getPlayerTime, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
//歌曲名称  

@Override public java.lang.String getTitle() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getTitle, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
//歌手名称

@Override public java.lang.String getSinger() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getSinger, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
//音乐时长

@Override public long getDulcation() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getDulcation, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
//是否暂停 

@Override public boolean isPause() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isPause, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
//是否启动 

@Override public boolean isStart() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isStart, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public android.graphics.Bitmap getBit() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.graphics.Bitmap _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getBit, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = android.graphics.Bitmap.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_getPlayerTime = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_getTitle = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getSinger = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_getDulcation = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_isPause = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_isStart = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_getBit = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
}
//当前时间

public long getPlayerTime() throws android.os.RemoteException;
//歌曲名称  

public java.lang.String getTitle() throws android.os.RemoteException;
//歌手名称

public java.lang.String getSinger() throws android.os.RemoteException;
//音乐时长

public long getDulcation() throws android.os.RemoteException;
//是否暂停 

public boolean isPause() throws android.os.RemoteException;
//是否启动 

public boolean isStart() throws android.os.RemoteException;
public android.graphics.Bitmap getBit() throws android.os.RemoteException;
}
