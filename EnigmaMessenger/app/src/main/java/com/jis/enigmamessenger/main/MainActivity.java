package com.jis.enigmamessenger.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import com.jis.enigmamessenger.adapter.MessagesAdapter;
import com.jis.enigmamessenger.common.BaseActivity;
import com.jis.enigmamessenger.R;
import com.jis.enigmamessenger.common.Common;
import com.jis.enigmamessenger.common.Constants;
import com.jis.enigmamessenger.model.RoomEntity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends BaseActivity {

    @BindView(R.id.activity_message_list)
    RecyclerView ui_recycler;

    @BindView(R.id.activity_main_edt_search)
    EditText ui_edtSearch;

    MessagesAdapter mAdapter;

    ArrayList<RoomEntity> mAllRooms = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        loadLayout();
    }

    private void loadLayout() {

        ui_recycler.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new MessagesAdapter(MainActivity.this);
        ui_recycler.setAdapter(mAdapter);
        mAdapter.setDatas(mAllRooms);

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
    }

    public void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE
        );
        imm.hideSoftInputFromWindow(ui_edtSearch.getWindowToken(), 0);
    }

    private void refresh() {

        mAllRooms.clear();
        mAllRooms.addAll(Common.g_rooms);

        Collections.sort(mAllRooms, new Comparator<RoomEntity>() {
            @Override
            public int compare(RoomEntity roomEntity, RoomEntity t1) {
                return t1.getLastDate().compareTo(roomEntity.getLastDate());
            }
        });

        if (ui_edtSearch.getText().toString().isEmpty()) {
            mAdapter.setDatas(mAllRooms);
        } else {
            search(ui_edtSearch.getText().toString());
        }
    }

    private void search(String keywords) {

        if (keywords.isEmpty()) {
            mAdapter.setDatas(mAllRooms);
            return;
        }

        ArrayList<RoomEntity> rooms = new ArrayList<>();
        for (RoomEntity one : mAllRooms) {

            if (one.getDisplayName().toLowerCase().contains(keywords.toLowerCase())) {
                rooms.add(one);
            }
        }

        mAdapter.setDatas(rooms);
    }

    private void gotoContacts() {

        if (Common.g_isAuthenticated) {
            Common.g_fromOther = true;
            Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
            startActivity(intent);
        } else {
            Common.g_fromOther = true;
            Intent intent = new Intent(MainActivity.this, FingerprintAuthActivity.class);
            intent.putExtra(Constants.TOWHERE, Constants.TOCONTACTS);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Common.g_main = this;
        loadRooms();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Common.g_main = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Common.g_isAppRunning = false;
        Common.g_isAuthenticated = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_edit) {
            gotoContacts();
        }

        return super.onOptionsItemSelected(item);
    }

    public void loadRooms() {

        OkHttpClient client =  new OkHttpClient();

        String url = Constants.BASE_URL + Constants.REQ_LOADROOMS;
        url += Common.encode(String.valueOf(Common.g_user.getId()));
        Request request = new Request.Builder().url(url).build();

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

                        Common.g_rooms.clear();

                        JSONArray rooms = jsonObject.getJSONArray(Constants.RES_ROOMINFOS);

                        for (int i = 0; i < rooms.length(); i++) {
                            JSONObject one = rooms.getJSONObject(i);
                            RoomEntity room = new RoomEntity(one);

                            if (!room.getMessage().isEmpty()) {
                                Common.g_rooms.add(room);
                            }
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refresh();
                            }
                        });

                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });
    }

    public void deleteRoom(RoomEntity roomEntity) {

        OkHttpClient client =  new OkHttpClient();

        String url = Constants.BASE_URL + Constants.REQ_DELETEROOM;

        String[] params = {roomEntity.getRoomName(), String.valueOf(Common.g_user.getId())};
        url += Common.encode(params);

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {


            }
        });

    }
}
