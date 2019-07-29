/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\sync\\MyDemo\\src\\com\\li\\demo\\service\\ITakePicRemote.aidl
 */
package com.li.demo.service;
public interface ITakePicRemote extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.li.demo.service.ITakePicRemote
{
private static final java.lang.String DESCRIPTOR = "com.li.demo.service.ITakePicRemote";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.li.demo.service.ITakePicRemote interface,
 * generating a proxy if needed.
 */
public static com.li.demo.service.ITakePicRemote asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.li.demo.service.ITakePicRemote))) {
return ((com.li.demo.service.ITakePicRemote)iin);
}
return new com.li.demo.service.ITakePicRemote.Stub.Proxy(obj);
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
case TRANSACTION_register:
{
data.enforceInterface(DESCRIPTOR);
this.register();
reply.writeNoException();
return true;
}
case TRANSACTION_unregister:
{
data.enforceInterface(DESCRIPTOR);
this.unregister();
reply.writeNoException();
return true;
}
case TRANSACTION_takePicture:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
com.li.demo.service.PictureListener _arg2;
_arg2 = com.li.demo.service.PictureListener.Stub.asInterface(data.readStrongBinder());
this.takePicture(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.li.demo.service.ITakePicRemote
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
@Override public void register() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_register, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void unregister() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_unregister, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void takePicture(int w, int h, com.li.demo.service.PictureListener pl) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(w);
_data.writeInt(h);
_data.writeStrongBinder((((pl!=null))?(pl.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_takePicture, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_register = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_unregister = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_takePicture = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
public void register() throws android.os.RemoteException;
public void unregister() throws android.os.RemoteException;
public void takePicture(int w, int h, com.li.demo.service.PictureListener pl) throws android.os.RemoteException;
}
