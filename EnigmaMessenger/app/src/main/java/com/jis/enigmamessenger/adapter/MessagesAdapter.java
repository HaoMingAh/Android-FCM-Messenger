package com.jis.enigmamessenger.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.jis.enigmamessenger.R;
import com.jis.enigmamessenger.common.Common;
import com.jis.enigmamessenger.common.Constants;
import com.jis.enigmamessenger.main.FingerprintAuthActivity;
import com.jis.enigmamessenger.main.MainActivity;
import com.jis.enigmamessenger.main.SignInActivity;
import com.jis.enigmamessenger.message.MessageActivity;
import com.jis.enigmamessenger.model.RoomEntity;
import com.jis.enigmamessenger.model.UserEntity;
import com.tubb.smrv.SwipeHorizontalMenuLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageHolder>  {

    private MainActivity _context;
    private ArrayList<RoomEntity> _rooms = new ArrayList<>();

    public MessagesAdapter(MainActivity c) {
        _context = c;
    }

    public void setDatas(ArrayList<RoomEntity> rooms) {
        _rooms = rooms;
        notifyDataSetChanged();
    }

    @Override
    public MessageHolder onCreateViewHolder(ViewGroup viewGroup, final int position) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_message_room, viewGroup, false);

        MessageHolder viewHolder = new MessageHolder(view);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final MessageHolder viewHolder, int i) {

        final RoomEntity roomEntity = _rooms.get(i);

        if (Common.g_isAuthenticated) {
            viewHolder.txvName.setText(roomEntity.getDisplayName());
            viewHolder.txvMessage.setText(roomEntity.getMessage());
        } else {
            viewHolder.txvName.setText(Common.getCircleString(roomEntity.getDisplayName().length()));
            viewHolder.txvMessage.setText(Common.getRectString(roomEntity.getMessage().length()));
        }

        if (roomEntity.getLastDate() != null)
            viewHolder.txvTime.setText(TimeAgo.using(roomEntity.getLastDate().getTime()));

        if (roomEntity.getUnread() == 0) {
            viewHolder.imvUnread.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.imvUnread.setVisibility(View.VISIBLE);
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UserEntity user = new UserEntity(roomEntity.getTargetId(), roomEntity.getDisplayName(), roomEntity.getPhonenumber(), "");

                if (Common.g_isAuthenticated) {
                    Common.g_fromOther = true;
                    MessageActivity.open(_context, user);
                } else {
                    Common.g_fromOther = true;
                    Intent intent = new Intent(_context, FingerprintAuthActivity.class);
                    intent.putExtra(Constants.TOWHERE, Constants.TOMESSAGE);
                    intent.putExtra(Constants.INTENTUSER, user);
                    _context.startActivity(intent);
                }

            }
        });


        viewHolder.txvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Common.g_isAuthenticated) {

                    int pos = viewHolder.getAdapterPosition();

                    RoomEntity room = _rooms.get(pos);
                    _context.deleteRoom(room);

                    _rooms.remove(pos);
                    Common.g_rooms.remove(room);
                    notifyItemRemoved(pos);



                } else {
                    viewHolder.smlSwipe.smoothCloseMenu(0);
                    Common.g_fromOther = true;
                    Intent intent = new Intent(_context, FingerprintAuthActivity.class);
                    _context.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return _rooms.size();
    }


    public class MessageHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_message_imv_profile)
        ImageView imvPhoto;

        @BindView(R.id.item_message_txv_name)
        TextView txvName;

        @BindView(R.id.item_message_txv_content)
        TextView txvMessage;

        @BindView(R.id.item_message_txv_time)
        TextView txvTime;

        @BindView(R.id.item_message_imv_unread)
        ImageView imvUnread;

        @BindView(R.id.txv_delete)
        TextView txvDelete;

        @BindView(R.id.sml)
        SwipeHorizontalMenuLayout smlSwipe;

        public MessageHolder(View view) {

            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
