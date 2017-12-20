package com.jis.enigmamessenger.main;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jis.enigmamessenger.R;
import com.jis.enigmamessenger.adapter.ContactsAdapter;
import com.jis.enigmamessenger.common.BaseActivity;
import com.jis.enigmamessenger.common.Common;
import com.jis.enigmamessenger.common.Constants;
import com.jis.enigmamessenger.model.ContactEntity;
import com.jis.enigmamessenger.model.UserEntity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
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

public class ContactsActivity extends BaseActivity {

    @BindView(R.id.activity_contact_list)
    RecyclerView ui_recycler;

    @BindView(R.id.activity_contacts_edt_search)
    EditText ui_edtSearch;

    ContactsAdapter _adapter;

    ArrayList<UserEntity> _allUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        ButterKnife.bind(this);

        loadLayout();
    }

    private void loadLayout() {

        ui_recycler.setLayoutManager(new LinearLayoutManager(this));
        _adapter = new ContactsAdapter(ContactsActivity.this);
        ui_recycler.setAdapter(_adapter);

        ui_edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                search(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        ui_edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    hideSoftKeyboard();
                }
                return false;
            }
        });

        refresh();
    }

    private void search(String keywords) {

        if (keywords.isEmpty()) {
            _adapter.setDatas(_allUsers);
            return;
        }

        ArrayList<UserEntity> users = new ArrayList<>();
        for (UserEntity one : _allUsers) {

            if (one.getName().toLowerCase().contains(keywords.toLowerCase()) ||
                    one.getPhoneNumber().contains(keywords.toLowerCase())) {
                users.add(one);
            }
        }

        _adapter.setDatas(users);
    }

    public void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE
        );
        imm.hideSoftInputFromWindow(ui_edtSearch.getWindowToken(), 0);
    }

    private void gotoContacts() {

        Common.g_fromOther = true;
        Intent intent= new Intent(Intent.ACTION_INSERT,  ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, 100);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.contacts_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_plus) {
            gotoContacts();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPermission();
    }

    private void refresh() {

        _allUsers.clear();
        _allUsers.addAll(Common.g_users);

        if (ui_edtSearch.getText().toString().isEmpty()) {
            _adapter.setDatas(_allUsers);
        } else {
            search(ui_edtSearch.getText().toString());
        }


    }

    public void saveContacts() {

        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);

        while (phones.moveToNext()) {

            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            ContactEntity contact = new ContactEntity(name, phoneNumber.replace(" ", "").replace("-", "").replace("(", "").replace(")", ""));
            if (!Common.g_contacts.contains(contact))
                Common.g_contacts.add(contact);

        }

        phones.close();

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<ContactEntity>>() {}.getType();
        final String json = gson.toJson(Common.g_contacts, type);

        if (Common.g_contacts.size() == 0) {
            return;
        }

        OkHttpClient client =  new OkHttpClient();

        String url = Constants.BASE_URL + Constants.REQ_SAVECONTACTS;

        final RequestBody body = new FormBody.Builder()
                .add(Constants.PARAM_ID, String.valueOf(Common.g_user.getId()))
                .add(Constants.PARAM_CONTACTS, json)
                .build();

        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                try {

                    final String strResponse = response.body().string();

                    JSONObject jsonObject = new JSONObject(strResponse);
                    int resultCode =  jsonObject.getInt(Constants.RES_CODE);

                    if (resultCode == Constants.CODE_SUCCESS) {

                        JSONArray contacts = jsonObject.getJSONArray(Constants.RES_CONTACTINFOS);

                        boolean isChanged = false;

                        for (int i = 0; i < contacts.length(); i++) {
                            JSONObject one = contacts.getJSONObject(i);
                            UserEntity user = new UserEntity(one);

                            if (!Common.g_users.contains(user)) {
                                Common.g_users.add(user);
                                isChanged = true;
                            }
                        }

                        if (isChanged) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    refresh();
                                }
                            });
                        }

                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });
    }

    private void checkPermission() {

        PermissionManager.with(ContactsActivity.this)
                .permission(PermissionEnum.READ_CONTACTS)
                .askAgain(false)
                .callback(new SmartCallback() {
                    @Override
                    public void result(boolean allPermissionsGranted, boolean somePermissionsDeniedForever){

                        if (allPermissionsGranted) {
                            saveContacts();
                        }
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
