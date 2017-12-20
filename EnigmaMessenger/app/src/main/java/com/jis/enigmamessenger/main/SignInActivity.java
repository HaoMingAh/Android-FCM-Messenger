package com.jis.enigmamessenger.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jis.enigmamessenger.R;
import com.jis.enigmamessenger.common.BaseActivity;
import com.jis.enigmamessenger.common.Common;
import com.jis.enigmamessenger.common.Constants;
import com.jis.enigmamessenger.model.ContactEntity;
import com.jis.enigmamessenger.model.UserEntity;
import com.pixplicity.easyprefs.library.Prefs;
import com.rengwuxian.materialedittext.MaterialEditText;

import net.rimoto.intlphoneinput.IntlPhoneInput;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rebus.permissionutils.PermissionEnum;
import rebus.permissionutils.PermissionManager;
import rebus.permissionutils.SmartCallback;

/**
 * Created by JIS on 6/6/2017.
 */

public class SignInActivity extends BaseActivity {

    @BindView(R.id.my_phone_input)
    IntlPhoneInput ui_inputPhone;

    @BindView(R.id.txv_verification_code)
    MaterialEditText ui_edtCode;

    @BindView(R.id.btn_sendcode)
    Button ui_btnSend;

    @BindView(R.id.btn_continue)
    Button ui_btnContinue;

    @BindView(R.id.container)
    LinearLayout ui_container;

    String _phoneNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        ButterKnife.bind(this);

        checkPermission();

        loadLayout();
    }

    private void loadLayout() {

        hideContinue(true);

        ui_container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });
    }

    private void hideContinue(boolean yesno) {

        if (yesno) {
            ui_edtCode.setVisibility(View.GONE);
            ui_btnContinue.setVisibility(View.GONE);
        } else {
            ui_edtCode.setVisibility(View.VISIBLE);
            ui_btnContinue.setVisibility(View.VISIBLE);
        }
    }

    private void hideKeyboard() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(ui_edtCode.getWindowToken(), 0);
    }

    private void onSuccess() {

        sendRegistrationToServer();
    }

    private void gotoMain() {

        Intent intent = new Intent(SignInActivity.this, FingerprintAuthActivity.class);
        intent.putExtra(Constants.TOWHERE, Constants.TOMAIN);
        startActivity(intent);
        finish();
    }

    @OnClick(R.id.btn_sendcode)
    void onSendCode() {

        if(ui_inputPhone.isValid()) {

            _phoneNumber = ui_inputPhone.getNumber();
            hideKeyboard();
            sendCode();

        } else {
            showErrorAlert(R.string.app_name, R.string.input_valid_phone);
        }

    }

    @OnClick(R.id.btn_continue)
    void onContinue() {

        hideKeyboard();

        if (ui_edtCode.getText().length() != 6) {
            showErrorAlert(R.string.app_name, R.string.input_valid_code);
            return;
        }

        register();
    }


    private void sendCode() {

        showProgress();

        OkHttpClient client =  new OkHttpClient();

        String url = Constants.BASE_URL + Constants.REQ_SENDCODE;
        String params = _phoneNumber;

        url += Common.encode(params);

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                closeProgress();
                showAlert(getString(R.string.oops), getString(R.string.connect_failed));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                closeProgress();

                String strResponse = response.body().string();

                try {
                    JSONObject jsonObject = new JSONObject(strResponse);
                    int resultCode =  jsonObject.getInt(Constants.RES_CODE);

                    if (resultCode == Constants.CODE_SUCCESS) {

                        runOnUiThread(new Runnable() {
                            public void run() {
                                ui_btnSend.setText(getString(R.string.resend));
                                hideContinue(false);
                            }
                        });

                    } else {
                        showErrorAlert(getString(R.string.oops), getString(R.string.wrong));
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

    }

    private void register() {

        showProgress();

        OkHttpClient client =  new OkHttpClient();

        String url = Constants.BASE_URL + Constants.REQ_REGISTER;

        String code = ui_edtCode.getText().toString();
        String params[] = {_phoneNumber, code, "0"};

        url += Common.encode(params);

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                closeProgress();
                showAlert(getString(R.string.oops), getString(R.string.connect_failed));

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                try {

                    final String strResponse = response.body().string();

                    JSONObject jsonObject = new JSONObject(strResponse);
                    int resultCode =  jsonObject.getInt(Constants.RES_CODE);

                    if (resultCode == Constants.CODE_SUCCESS) {  // exist user

                        int userId = jsonObject.getInt(Constants.RES_USERID);
                        Common.g_user = new UserEntity(userId);
                        Prefs.putInt(Constants.PREFKEY_ID, userId);
                        getContacts();

                    } else if (resultCode == 101) {
                        closeProgress();
                        showAlert(getString(R.string.oops), getString(R.string.wrong_phone));
                    } else if (resultCode == 102) {
                        closeProgress();
                        showAlert(getString(R.string.oops), getString(R.string.input_valid_code));
                    } else if (resultCode == 200) {     // register

                        int userId = jsonObject.getInt(Constants.RES_USERID);
                        Common.g_user = new UserEntity(userId);
                        Prefs.putInt(Constants.PREFKEY_ID, userId);
                        saveContacts();
                    } else {
                        closeProgress();
                        showAlert(getString(R.string.oops), getString(R.string.connect_failed));
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

    }

    public void getContacts() {

        OkHttpClient client =  new OkHttpClient();

        String url = Constants.BASE_URL + Constants.REQ_GETCONTACTS;

        url += Common.encode(String.valueOf(Common.g_user.getId()));

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                closeProgress();
                showAlert(getString(R.string.oops), getString(R.string.connect_failed));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                closeProgress();

                try {

                    final String strResponse = response.body().string();

                    JSONObject jsonObject = new JSONObject(strResponse);
                    int resultCode =  jsonObject.getInt(Constants.RES_CODE);

                    if (resultCode == Constants.CODE_SUCCESS) {

                        JSONArray contacts = jsonObject.getJSONArray(Constants.RES_CONTACTINFOS);

                        Common.g_users.clear();
                        for (int i = 0; i < contacts.length(); i++) {
                            JSONObject one = contacts.getJSONObject(i);
                            UserEntity user = new UserEntity(one);

                            if (!Common.g_users.contains(user)) {
                                Common.g_users.add(user);
                            }
                        }

                        onSuccess();

                    } else {
                        showAlert(getString(R.string.oops), getString(R.string.connect_failed));
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });
    }

    public void saveContacts() {

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<ContactEntity>>() {}.getType();
        final String json = gson.toJson(Common.g_contacts, type);

        if (Common.g_contacts.size() == 0) {
            closeProgress();
            onSuccess();
            return;
        }

        OkHttpClient client =  new OkHttpClient();

        String url = Constants.BASE_URL + Constants.REQ_SAVECONTACTS;

        RequestBody body = new FormBody.Builder()
                .add(Constants.PARAM_ID, String.valueOf(Common.g_user.getId()))
                .add(Constants.PARAM_CONTACTS, json)
                .build();

        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                closeProgress();
                showAlert(getString(R.string.oops), getString(R.string.connect_failed));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                closeProgress();

                try {

                    final String strResponse = response.body().string();

                    JSONObject jsonObject = new JSONObject(strResponse);
                    int resultCode =  jsonObject.getInt(Constants.RES_CODE);

                    if (resultCode == Constants.CODE_SUCCESS) {

                        JSONArray contacts = jsonObject.getJSONArray(Constants.RES_CONTACTINFOS);

                        Common.g_users.clear();

                        for (int i = 0; i < contacts.length(); i++) {
                            JSONObject one = contacts.getJSONObject(i);
                            UserEntity user = new UserEntity(one);

                            if (!Common.g_users.contains(user)) {
                                Common.g_users.add(user);
                            }
                        }

                        onSuccess();

                    } else {
                        showAlert(getString(R.string.oops), getString(R.string.connect_failed));
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });
    }

    private void sendRegistrationToServer() {

        String token = Prefs.getString(Constants.PREFKEY_TOKEN, "");

        if (token.isEmpty()) {
            gotoMain();
            return;
        }

        OkHttpClient client =  new OkHttpClient();

        String url = Constants.BASE_URL + Constants.REQ_REGISTERTOKEN;

        String id = String.valueOf(Common.g_user.getId());
        String[] params = {id, token};

        url += Common.encode(params);

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                gotoMain();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                gotoMain();
            }
        });
    }

    private void loadContacts() {

        if (ContextCompat.checkSelfPermission(_context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);

        while (phones.moveToNext()) {

            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            ContactEntity contact = new ContactEntity(name, phoneNumber.replace(" ", "").replace("-", "").replace("(", "").replace(")", ""));

            if (!Common.g_contacts.contains(contact))
                Common.g_contacts.add(contact);

        }

        phones.close();
    }

    private void checkPermission() {

        PermissionManager.with(SignInActivity.this)
                .permission(PermissionEnum.READ_CONTACTS)
                .askAgain(false)
                .callback(new SmartCallback() {
                    @Override
                    public void result(boolean allPermissionsGranted, boolean somePermissionsDeniedForever){
                        if (allPermissionsGranted)
                            loadContacts();
                    }
                })
                .ask();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.handleResult(requestCode, permissions, grantResults);
    }

}
