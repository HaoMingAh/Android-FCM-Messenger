package com.jis.enigmamessenger.common;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.jis.enigmamessenger.R;
import com.jis.enigmamessenger.main.FingerprintAuthActivity;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class BaseActivity extends AppCompatActivity {

    public Context _context = null;

    private ProgressDialog _progressDlg;
    private Vibrator _vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _context = this;
        _vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();

         //for test
        if (!Common.g_fromOther && Common.g_isAuthenticated) {

            Common.g_fromOther = true;
            Intent intent = new Intent(BaseActivity.this, FingerprintAuthActivity.class);
            intent.putExtra(Constants.NEED_AUTH, true);
            startActivity(intent);
        } else {
            Common.g_fromOther = false;
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Common.g_fromOther = true;
    }

    @Override
    protected void onDestroy() {

        closeProgress();

        try {
            if (_vibrator != null)
                _vibrator.cancel();
        } catch (Exception e) {
        }
        _vibrator = null;

        super.onDestroy();
    }


    public void showProgress(boolean cancelable) {

        if (_progressDlg != null)
            return;

        try {
            _progressDlg = new ProgressDialog(_context, R.style.MyDialogTheme);
            _progressDlg.setCancelable(cancelable);
            _progressDlg.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
            _progressDlg.show();

        } catch (Exception e) {
        }
    }

    public void showProgress() {
        showProgress(false);
    }

    public void closeProgress() {

        if(_progressDlg == null) {
            return;
        }

        _progressDlg.dismiss();
        _progressDlg = null;
    }

    public void showAlert(int title, int msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(_context);
        builder.setMessage(msg)
                .setTitle(title)
                .setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void showAlert(final String title, final String msg) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new SweetAlertDialog(BaseActivity.this)
                        .setTitleText(title)
                        .setContentText(msg)
                        .show();
            }
        });

    }

    public void showErrorAlert(final String title, final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new SweetAlertDialog(BaseActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(title)
                        .setContentText(msg)
                        .show();
            }
        });
    }

    public void showErrorAlert(int title, int msg) {

        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(getString(title))
                .setContentText(getString(msg))
                .show();
    }

    public void showSuccessAlert(String title, String msg) {

        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(title)
                .setContentText(msg)
                .show();
    }

    /**
     *  show toast
     * @param toast_string
     */
    public void showToast(String toast_string) {
        Toast.makeText(_context, toast_string, Toast.LENGTH_SHORT).show();
    }

    public void vibrate() {

        if (_vibrator != null)
            _vibrator.vibrate(500);
    }




}
