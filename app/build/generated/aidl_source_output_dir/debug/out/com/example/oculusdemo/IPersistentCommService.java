/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: C:\Users\siweiii\AppData\Local\Android\Sdk\build-tools\35.0.0\aidl.exe -pC:\Users\siweiii\AppData\Local\Android\Sdk\platforms\android-34\framework.aidl -oC:\project\android\app\build\generated\aidl_source_output_dir\debug\out -IC:\project\android\app\src\main\aidl -IC:\project\android\app\src\debug\aidl -IC:\Users\siweiii\.gradle\caches\8.13\transforms\48fc52ae1c984a47a4cc3bfa9d303ebd\transformed\core-1.13.1\aidl -IC:\Users\siweiii\.gradle\caches\8.13\transforms\57ef6dedd2f8a3d504957e1493cbbc60\transformed\versionedparcelable-1.1.1\aidl -dC:\Users\siweiii\AppData\Local\Temp\aidl17503154277874510994.d C:\project\android\app\src\main\aidl\com\example\oculusdemo\IPersistentCommService.aidl
 */
package com.example.oculusdemo;
public interface IPersistentCommService extends android.os.IInterface
{
  /** Default implementation for IPersistentCommService. */
  public static class Default implements com.example.oculusdemo.IPersistentCommService
  {
    @Override public void startSession() throws android.os.RemoteException
    {
    }
    @Override public void stopSession() throws android.os.RemoteException
    {
    }
    @Override public void sendMessage(java.lang.String message) throws android.os.RemoteException
    {
    }
    @Override public void registerCallback(com.example.oculusdemo.ILogCallback callback) throws android.os.RemoteException
    {
    }
    @Override public void unregisterCallback(com.example.oculusdemo.ILogCallback callback) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.example.oculusdemo.IPersistentCommService
  {
    /** Construct the stub at attach it to the interface. */
    @SuppressWarnings("this-escape")
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.example.oculusdemo.IPersistentCommService interface,
     * generating a proxy if needed.
     */
    public static com.example.oculusdemo.IPersistentCommService asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.example.oculusdemo.IPersistentCommService))) {
        return ((com.example.oculusdemo.IPersistentCommService)iin);
      }
      return new com.example.oculusdemo.IPersistentCommService.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(descriptor);
      }
      if (code == INTERFACE_TRANSACTION) {
        reply.writeString(descriptor);
        return true;
      }
      switch (code)
      {
        case TRANSACTION_startSession:
        {
          this.startSession();
          reply.writeNoException();
          break;
        }
        case TRANSACTION_stopSession:
        {
          this.stopSession();
          reply.writeNoException();
          break;
        }
        case TRANSACTION_sendMessage:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          this.sendMessage(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_registerCallback:
        {
          com.example.oculusdemo.ILogCallback _arg0;
          _arg0 = com.example.oculusdemo.ILogCallback.Stub.asInterface(data.readStrongBinder());
          this.registerCallback(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_unregisterCallback:
        {
          com.example.oculusdemo.ILogCallback _arg0;
          _arg0 = com.example.oculusdemo.ILogCallback.Stub.asInterface(data.readStrongBinder());
          this.unregisterCallback(_arg0);
          reply.writeNoException();
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements com.example.oculusdemo.IPersistentCommService
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
      @Override public void startSession() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_startSession, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void stopSession() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_stopSession, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void sendMessage(java.lang.String message) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(message);
          boolean _status = mRemote.transact(Stub.TRANSACTION_sendMessage, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void registerCallback(com.example.oculusdemo.ILogCallback callback) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeStrongInterface(callback);
          boolean _status = mRemote.transact(Stub.TRANSACTION_registerCallback, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void unregisterCallback(com.example.oculusdemo.ILogCallback callback) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeStrongInterface(callback);
          boolean _status = mRemote.transact(Stub.TRANSACTION_unregisterCallback, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_startSession = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_stopSession = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_sendMessage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_registerCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_unregisterCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
  }
  /** @hide */
  public static final java.lang.String DESCRIPTOR = "com.example.oculusdemo.IPersistentCommService";
  public void startSession() throws android.os.RemoteException;
  public void stopSession() throws android.os.RemoteException;
  public void sendMessage(java.lang.String message) throws android.os.RemoteException;
  public void registerCallback(com.example.oculusdemo.ILogCallback callback) throws android.os.RemoteException;
  public void unregisterCallback(com.example.oculusdemo.ILogCallback callback) throws android.os.RemoteException;
}
