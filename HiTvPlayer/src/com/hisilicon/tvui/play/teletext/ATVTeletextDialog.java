package com.hisilicon.tvui.play.teletext;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;

import com.hisilicon.android.tvapi.impl.SourceManagerImpl;
import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.play.EnCMDCode;
import com.hisilicon.dtv.play.Player;
import com.hisilicon.dtv.play.TeletextControl;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;

public class ATVTeletextDialog extends Dialog {
    private static final String TAG = "ATVTeletextDialog";

    private TeletextControl mTeletextControl = null;
    private SourceManagerImpl sourceManager;

    public ATVTeletextDialog(Context context, int theme) {
        super(context, theme);
        Context mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogTool.d(LogTool.MPLAY, "TTX:onCreate");
        super.onCreate(savedInstanceState);
        sourceManager = SourceManagerImpl.getInstance();
        sourceManager.showTTX(1);
        LogTool.d(LogTool.MPLAY, "showTTX");
    }

    @Override
    protected void onStart() {
        LogTool.d(LogTool.MPLAY, "TTX:onStart");
        super.onStart();
    }

    @Override
    protected void onStop() {
        LogTool.d(LogTool.MPLAY, "TTX:onStop");
        super.onStop();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        LogTool.d(LogTool.MPLAY, "TTX:KeyEvent.onKeyUp:keyCode = " + keyCode);
        if (sourceManager.isTTXVisible()) {
            LogTool.d(LogTool.MPLAY, "sourceManager.isTTXVisible()  true" );
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogTool.d(LogTool.MPLAY, "TTX:KeyEvent.ACTION_DOWN:keyCode = " + keyCode);
        if (sourceManager.isTTXVisible()){
            switch (keyCode) {
                case KeyValue.DTV_KEYVALUE_BACK:
                case KeyValue.DTV_KEYVALUE_TXT: {
                    LogTool.d(LogTool.MPLAY, "ATV_KEYVALUE_TXT: showTTX(false)");
                    sourceManager.showTTX(0);
                    this.dismiss();
                    break;
                }
                case KeyValue.DTV_KEYVALUE_0: {
                    sourceManager.setTTXCommand(0);
                    break;
                }
                case KeyValue.DTV_KEYVALUE_1: {
                    sourceManager.setTTXCommand(1);
                    break;
                }
                case KeyValue.DTV_KEYVALUE_2: {
                    sourceManager.setTTXCommand(2);
                    break;
                }
                case KeyValue.DTV_KEYVALUE_3: {
                    sourceManager.setTTXCommand(3);
                    break;
                }
                case KeyValue.DTV_KEYVALUE_4: {
                    sourceManager.setTTXCommand(4);
                    break;
                }
                case KeyValue.DTV_KEYVALUE_5: {
                    sourceManager.setTTXCommand(5);
                    break;
                }
                case KeyValue.DTV_KEYVALUE_6: {
                    sourceManager.setTTXCommand(6);
                    break;
                }
                case KeyValue.DTV_KEYVALUE_7: {
                    sourceManager.setTTXCommand(7);
                    break;
                }
                case KeyValue.DTV_KEYVALUE_8: {
                    sourceManager.setTTXCommand(8);
                    break;
                }
                case KeyValue.DTV_KEYVALUE_9: {
                    sourceManager.setTTXCommand(9);
                    break;
                }
                case KeyValue.DTV_KEYVALUE_DPAD_UP:
                case KeyValue.DTV_KEYVALUE_PAGEUP: {
                    sourceManager.setTTXCommand(10);
                    break;
                }
                case KeyValue.DTV_KEYVALUE_DPAD_DOWN:
                case KeyValue.DTV_KEYVALUE_PAGEDOWN: {
                    sourceManager.setTTXCommand(11);
                    break;
                }
                case KeyValue.DTV_KEYVALUE_DPAD_LEFT: {
                    sourceManager.setTTXCommand(12);
                    break;
                }
                case KeyValue.DTV_KEYVALUE_DPAD_RIGHT: {
                    sourceManager.setTTXCommand(13);
                    break;
                }
                case KeyValue.DTV_KEYVALUE_RED: {
                    sourceManager.setTTXCommand(16);
                    break;
                }
                case KeyValue.DTV_KEYVALUE_GREEN: {
                    sourceManager.setTTXCommand(17);
                    break;
                }
                case KeyValue.DTV_KEYVALUE_YELLOW: {
                    sourceManager.setTTXCommand(18);
                    return true;
                }
                case KeyValue.DTV_KEYVALUE_BLUE: {
                    sourceManager.setTTXCommand(19);
                    return true;
                }
                case KeyValue.DTV_KEYVALUE_MEDIA_PLAY_PAUSE: {
                    sourceManager.setTTXCommand(20);
                    break;
                }
                case KeyValue.DTV_KEYVALUE_CC: {
                    sourceManager.setTTXCommand(21);
                    break;
                }
                case KeyValue.DTV_KEYVALUE_HOLD: {
                    sourceManager.setTTXCommand(22);
                    return true;
                }
                case KeyValue.DTV_KEYVALUE_MEDIA_REWIND: {
                    sourceManager.setTTXCommand(23);
                    break;
                }
                case KeyValue.DTV_KEYVALUE_INFOBAR: {
                    sourceManager.setTTXCommand(24);
                    break;
                }
                case KeyValue.DTV_KEYVALUE_SIZE: {
                    sourceManager.setTTXCommand(25);
                    break;
                }
                case KeyValue.DTV_KEYVALUE_MEDIA_STOP: {
                    sourceManager.setTTXCommand(26);
                    break;
                }
                default: {
                    break;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
