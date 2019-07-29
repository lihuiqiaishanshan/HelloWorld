/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\sync\\MyDemo\\src\\com\\rockitv\\android\\IRemote.aidl
 */
package com.rockitv.android;
public interface IRemote extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.rockitv.android.IRemote
{
private static final java.lang.String DESCRIPTOR = "com.rockitv.android.IRemote";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.rockitv.android.IRemote interface,
 * generating a proxy if needed.
 */
public static com.rockitv.android.IRemote asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.rockitv.android.IRemote))) {
return ((com.rockitv.android.IRemote)iin);
}
return new com.rockitv.android.IRemote.Stub.Proxy(obj);
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
case TRANSACTION_getVideoByChannel:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _result = this.getVideoByChannel(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getEpgByChannel:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _result = this.getEpgByChannel(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.rockitv.android.IRemote
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
//根据频道得到当前频道所播放内容的相关视频列表，返回一个json格式的文本
//json格式如下{"title","当前所播放节目标题","videos":[{"title","相关视频标题","img","相关视频图片","url":"相关视频网址"},....]}

@Override public java.lang.String getVideoByChannel(java.lang.String channel) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(channel);
mRemote.transact(Stub.TRANSACTION_getVideoByChannel, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
//根据频道得到当前频道的EPG，返回一个json格式的文本
//json格式如下{"time","当前时间","epg":[{"节目时间\名称"},....]}

@Override public java.lang.String getEpgByChannel(java.lang.String channel) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(channel);
mRemote.transact(Stub.TRANSACTION_getEpgByChannel, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_getVideoByChannel = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_getEpgByChannel = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
//根据频道得到当前频道所播放内容的相关视频列表，返回一个json格式的文本
//json格式如下{"title","当前所播放节目标题","videos":[{"title","相关视频标题","img","相关视频图片","url":"相关视频网址"},....]}

public java.lang.String getVideoByChannel(java.lang.String channel) throws android.os.RemoteException;
//根据频道得到当前频道的EPG，返回一个json格式的文本
//json格式如下{"time","当前时间","epg":[{"节目时间\名称"},....]}

public java.lang.String getEpgByChannel(java.lang.String channel) throws android.os.RemoteException;
}
