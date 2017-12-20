package com.jis.enigmamessenger.main;

import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.jis.enigmamessenger.R;
import com.jis.enigmamessenger.common.BaseActivity;
import com.jis.enigmamessenger.common.Common;
import com.jis.enigmamessenger.common.Constants;
import com.jis.enigmamessenger.message.MessageActivity;
import com.jis.enigmamessenger.model.UserEntity;
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify;
import com.wei.android.lib.fingerprintidentify.base.BaseFingerprint;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by JIS on 6/12/2017.
 */

public class FingerprintAuthActivity extends BaseActivity {

    @BindView(R.id.btn_continue)
    Button ui_btnContinue;

    @BindView(R.id.txv_note)
    TextView ui_txvNote;

    @BindView(R.id.imv_finger)
    ImageView ui_imvFinger;

    FingerprintIdentify mFingerprintIdentify;

    int _toWhere = -1;
    UserEntity _user = null;
    boolean _needAuth = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger);

        ButterKnife.bind(this);

        ui_imvFinger.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary));

        _toWhere = getIntent().getIntExtra(Constants.TOWHERE, -1);
        _user = (UserEntity) getIntent().getSerializableExtra(Constants.INTENTUSER);
        _needAuth = getIntent().getBooleanExtra(Constants.NEED_AUTH, false);
    }

    private void checkFinger() {

        mFingerprintIdentify = new FingerprintIdentify(this);

        boolean available = mFingerprintIdentify.isFingerprintEnable() && mFingerprintIdentify.isHardwareEnable() &&
                mFingerprintIdentify.isRegisteredFingerprint();

        if (available) {
            ui_txvNote.setText(getString(R.string.finger_available));
            ui_btnContinue.setVisibility(View.INVISIBLE);
        } else {
            ui_txvNote.setText(getString(R.string.finger_notavailable));
            ui_btnContinue.setText(getString(R.string.setting));
            ui_btnContinue.setVisibility(View.VISIBLE);
        }

        if (available) {

            mFingerprintIdentify.startIdentify(100, new BaseFingerprint.FingerprintIdentifyListener() {
                @Override
                public void onSucceed() {
                    // succeed, release hardware automatically

                    if (_needAuth) {
                        Common.g_fromOther = true;
                        finish();
                    } else {
                        Common.g_isAuthenticated = true;
                        Common.g_fromOther = true;
                        gotoNext();
                    }
                }

                @Override
                public void onNotMatch(int availableTimes) {
                    showToast(getString(R.string.auth_fail));
                }

                @Override
                public void onFailed() {
                    // failed, release hardware automatically
                    showToast(getString(R.string.auth_fail));
                }
            });
        }

    }

    private void gotoMain() {

        Intent intent = new Intent(FingerprintAuthActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void gotoContacts() {

        Intent intent = new Intent(FingerprintAuthActivity.this, ContactsActivity.class);
        startActivity(intent);
        finish();
    }

    private void gotoMessage() {

        MessageActivity.open(FingerprintAuthActivity.this, _user);
        finish();
    }

    private void gotoNext() {

        switch (_toWhere) {

            case Constants.TOMAIN:
                gotoMain();
                break;

            case Constants.TOCONTACTS:
                gotoContacts();
                break;

            case Constants.TOMESSAGE:
                gotoMessage();
                break;

            default:
                finish();
                break;
        }
    }

    private void gotoSetting() {

        startActivityForResult(new Intent(Settings.ACTION_SECURITY_SETTINGS), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkFinger();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @OnClick(R.id.btn_continue)
    void onContinue() {

        if (ui_btnContinue.getText().equals(getString(R.string.btn_continue))) {
            gotoNext();
        } else {
            gotoSetting();
        }
    }


}
