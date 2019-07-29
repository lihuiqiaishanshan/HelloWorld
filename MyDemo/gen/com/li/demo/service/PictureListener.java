/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\sync\\MyDemo\\src\\com\\li\\demo\\service\\PictureListener.aidl
 */
package com.li.demo.service;
public interface PictureListener extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.li.demo.service.PictureListener
{
private static final java.lang.String DESCRIPTOR = "com.li.demo.service.PictureListener";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.li.demo.service.PictureListener interface,
 * generating a proxy if needed.
 */
public static com.li.demo.service.PictureListener asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.li.demo.service.PictureListener))) {
return ((com.li.demo.service.PictureListener)iin);
}
return new com.li.demo.service.PictureListener.Stub.Proxy(obj);
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
case TRANSACTION_onChange:
{
data.enforceInterface(DESCRIPTOR);
android.graphics.Bitmap _arg0;
if ((0!=data.readInt())) {
_arg0 = android.graphics.Bitmap.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onChange(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.li.demo.service.PictureListener
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
@Override public void onChange(android.graphics.Bitmap bm) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((bm!=null)) {
_data.writeInt(1);
bm.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onChange, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onChange = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void onChange(android.graphics.Bitmap bm) throws android.os.RemoteException;
}
