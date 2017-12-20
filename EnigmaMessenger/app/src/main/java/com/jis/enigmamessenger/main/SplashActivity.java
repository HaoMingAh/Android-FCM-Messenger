package com.jis.enigmamessenger.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.jis.enigmamessenger.R;
import com.jis.enigmamessenger.common.BaseActivity;
import com.jis.enigmamessenger.common.Common;
import com.jis.enigmamessenger.common.Constants;
import com.jis.enigmamessenger.model.ContactEntity;
import com.jis.enigmamessenger.model.UserEntity;
import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rebus.permissionutils.PermissionEnum;
import rebus.permissionutils.PermissionManager;
import rebus.permissionutils.SmartCallback;


public class SplashActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        Common.g_isAppRunning = true;

        int userId = Prefs.getInt(Constants.PREFKEY_ID, 0);

        if (userId == 0) {
            gotoLogin();
            return;
        }

        checkPermission();
        login(userId);
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

    private void login(int userId) {

        Common.g_user = new UserEntity(userId);
        getContacts();
    }

    private void gotoLogin() {

        Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    private void gotoMain() {

        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();

    }

    public void getContacts() {

        OkHttpClient client =  new OkHttpClient();

        String url = Constants.BASE_URL + Constants.REQ_GETCONTACTS;

        url += Common.encode(String.valueOf(Common.g_user.getId()));

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                gotoLogin();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String strResponse = response.body().string();

                try {
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

                        gotoMain();

                    } else {
                        gotoLogin();
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });
    }

    private void checkPermission() {

        PermissionManager.with(SplashActivity.this)
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
