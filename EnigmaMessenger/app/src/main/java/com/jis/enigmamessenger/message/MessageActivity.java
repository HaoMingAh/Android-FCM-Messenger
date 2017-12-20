package com.jis.enigmamessenger.message;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.daasuu.bl.ArrowDirection;
import com.daasuu.bl.BubbleLayout;
import com.daasuu.bl.BubblePopupHelper;
import com.jis.enigmamessenger.R;
import com.jis.enigmamessenger.common.BaseActivity;
import com.jis.enigmamessenger.common.Common;
import com.jis.enigmamessenger.common.Constants;
import com.jis.enigmamessenger.model.RoomEntity;
import com.jis.enigmamessenger.model.UserEntity;
import com.orhanobut.logger.Logger;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

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

public class MessageActivity extends BaseActivity
        implements MessageInput.InputListener,
        MessageInput.AttachmentsListener,
        MessagesListAdapter.OnLoadMoreListener,
        MessagesListAdapter.OnMessageLongClickListener {

    @BindView(R.id.container)
    LinearLayout mContainer;

    @BindView(R.id.messagesList)
    MessagesList mMessagesList;

    @BindView(R.id.activity_message_input)
    MessageInput mMessageInput;

    @BindView(R.id.activity_message_imv_profile)
    ImageView mImvPartnerProfile;

    @BindView(R.id.activity_message_txv_name)
    TextView mTxvPartnerName;

    @BindView(R.id.activity_message_imv_phone)
    ImageView mImvPhone;


    protected String senderId = "";
    protected ImageLoader mImageLoader;
    protected MessagesListAdapter<Message> mAdapter;

    static MessageUser mPartner = null;
    static RoomEntity mRoomEntity = null;

    MessageUser mMine = null;
    String mRoomName = "";
    int mLoadedMessageCount = 0;

    static String mPartnerPhone = "";

    boolean _isLoading = false;

    public static void open(Context context, UserEntity partner) {

        Intent intent = new Intent(context, MessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        mPartner = new MessageUser(String.valueOf(partner.getId()), partner.getName(), partner.getProfile());
        mPartnerPhone = partner.getPhoneNumber();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        ButterKnife.bind(this);

        mMine = new MessageUser(String.valueOf(Common.g_user.getId()), Common.g_user.getName(), Common.g_user.getProfile());
        senderId = mMine.getId();
        mRoomName = makeRoomName();
        //mRoomEntity = Common.g_user.getRoomEntity(mRoomName);

        loadLayout();

        enterRoom();
        loadMessages(0);
        setRead();
    }

    private void loadLayout() {

        // partner
        //Picasso.with(this).load(mPartner.getAvatar()).placeholder(R.drawable.avatar_empty).into(mImvPartnerProfile);
        mTxvPartnerName.setText(mPartner.getName());

        mImageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
            Picasso.with(MessageActivity.this).load(url).placeholder(R.drawable.placeholder).into(imageView);
            }
        };

        initAdapter();

        MessageInput input = (MessageInput) findViewById(R.id.activity_message_input);
        input.setInputListener(this);
        input.setAttachmentsListener(this);


    }


    private void enterRoom() {

        OkHttpClient client =  new OkHttpClient();

        String url = Constants.BASE_URL + Constants.REQ_CREATEROOM;
        String[] params = {mMine.getId(), mPartner.getId()};
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

    private void initAdapter() {

        MessageHolders holdersConfig = new MessageHolders()
                .setIncomingTextLayout(R.layout.item_custom_incoming_text_message)
                .setOutcomingTextLayout(R.layout.item_custom_outcoming_text_message)
                .setIncomingImageLayout(R.layout.item_custom_incoming_image_message)
                .setOutcomingImageLayout(R.layout.item_custom_outcoming_image_message);

        mAdapter = new MessagesListAdapter<>(senderId, holdersConfig, mImageLoader);
        mAdapter.setLoadMoreListener(this);
        mAdapter.setOnMessageLongClickListener(this);
        mMessagesList.setAdapter(mAdapter);

    }



    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        loadMessages(mLoadedMessageCount);
    }

    @Override
    public void onMessageLongClick(IMessage message) {

//        BubbleLayout bubbleLayout = (BubbleLayout) LayoutInflater.from(this).inflate(R.layout.bubble_delete, null);
//        PopupWindow popupWindow = BubblePopupHelper.create(this, bubbleLayout);
//
//        bubbleLayout.setArrowDirection(ArrowDirection.BOTTOM);
//        popupWindow.showAtLocation(mContainer, Gravity.NO_GRAVITY, 100, 200);
    }

    public void loadMessages(int totalItemsCount) {

        if (_isLoading)
            return;

        _isLoading = true;

        OkHttpClient client =  new OkHttpClient();

        String url = Constants.BASE_URL + Constants.REQ_LOADMESSAGESBYUSER;

        String[] params = {mRoomName, String.valueOf(Common.g_user.getId()), String.valueOf(totalItemsCount)};
        url += Common.encode(params);
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                _isLoading = false;

                try {

                    final String strResponse = response.body().string();

                    JSONObject jsonObject = new JSONObject(strResponse);
                    int resultCode =  jsonObject.getInt(Constants.RES_CODE);

                    if (resultCode == Constants.CODE_SUCCESS) {

                        JSONArray msgs = jsonObject.getJSONArray(Constants.RES_MESSAGES);
                        final ArrayList<Message> newMessages = new ArrayList<>();

                        for (int i = 0; i < msgs.length(); i++) {
                            JSONObject one = msgs.getJSONObject(i);

                            String id = one.getString(Constants.RES_ID);
                            String sender = one.getString(Constants.RES_SENDER);
                            String body = one.getString(Constants.RES_MESSAGE);
                            String cratedAt = one.getString(Constants.RES_CREATEDAT);

                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            format.setTimeZone(TimeZone.getTimeZone("gmt"));
                            Date date = format.parse(cratedAt);

                            Message message;

                            if (sender.equals(mMine.getId()))
                                message = new Message(id, mMine, body, date, Message.TEXT_MESSAGE);
                            else
                                message = new Message(id, mPartner, body, date, Message.TEXT_MESSAGE);

                            newMessages.add(message);
                        }

                        if (newMessages.size() > 0) {

                            mLoadedMessageCount += newMessages.size();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.addToEnd(newMessages, false);
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


    public void setRead() {

        OkHttpClient client =  new OkHttpClient();

        String url = Constants.BASE_URL + Constants.REQ_READMESSAGE;
        String[] params = {mRoomName, mMine.getId()};
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

    public void sendMessage(String message) {

        OkHttpClient client =  new OkHttpClient();

        String url = Constants.BASE_URL + Constants.REQ_SENDMESSAGE;

        RequestBody body = new FormBody.Builder()
                .add(Constants.PARAM_SENDER, mMine.getId())
                .add(Constants.PARAM_TARGET, mPartner.getId())
                .add(Constants.PARAM_MESSAGE, message)
                .build();

        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Message txtMessage = new Message(mMine, message);
//                        mAdapter.addToStart(txtMessage, true);
//                        mLoadedMessageCount += 1;
//                    }
//                });
            }
        });

    }


    public void onReceiveMessage(String body, String type) {

        if (type.equals("text")) {
            Message txtMessage = new Message(mPartner, body);
            mAdapter.addToStart(txtMessage, true);
            mLoadedMessageCount += 1;

            if (mRoomEntity != null) {
                mRoomEntity.setMessage(body);
                mRoomEntity.setLastDate(new Date());
            }

        }

    }

    public MessageUser getTarget() {
        return mPartner;
    }

    @Override
    public boolean onSubmit(final CharSequence input) {

        Message txtMessage = new Message(mMine, input.toString());
        mAdapter.addToStart(txtMessage, true);
        mLoadedMessageCount += 1;

        sendMessage(input.toString());

        return true;
    }

    @Override
    public void onAddAttachments() {

    }




    @OnClick(R.id.activity_message_imv_phone)
    void onPhone() {

        Common.g_fromOther = true;
        String uri = "tel:" + mPartnerPhone.trim() ;
        Intent intent = new Intent(Intent.ACTION_DIAL );
        intent.setData(Uri.parse(uri));
        _context.startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setRead();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Common.g_messageActivity = this;
    }


    public String makeRoomName() {

        if (Integer.parseInt(mMine.getId()) < Integer.parseInt(mPartner.getId())) {
            return mMine.getId() + "_" + mPartner.getId();
        } else {
            return mPartner.getId() + "_" + mMine.getId();
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

        Common.g_messageActivity = null;
    }


}
