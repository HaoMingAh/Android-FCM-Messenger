package com.jis.enigmamessenger.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jis.enigmamessenger.R;
import com.jis.enigmamessenger.common.Common;
import com.jis.enigmamessenger.message.MessageActivity;
import com.jis.enigmamessenger.model.RoomEntity;
import com.jis.enigmamessenger.model.UserEntity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.MessageHolder>  {

    private Context _context;
    private ArrayList<UserEntity> _contacts = new ArrayList<>();

    public ContactsAdapter(Context c) {
        _context = c;
    }

    public void setDatas(ArrayList<UserEntity> contacts) {
        _contacts = contacts;
        notifyDataSetChanged();
    }

    @Override
    public MessageHolder onCreateViewHolder(ViewGroup viewGroup, final int position) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_contacts, viewGroup, false);

        MessageHolder viewHolder = new MessageHolder(view);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(MessageHolder viewHolder, int i) {

        final UserEntity userEntity = _contacts.get(i);

        viewHolder.txvName.setText(userEntity.getName());
        viewHolder.txvPhone.setText(userEntity.getPhoneNumber());

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.g_fromOther = true;
                MessageActivity.open(_context, userEntity);

            }
        });

        viewHolder.imvPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Common.g_fromOther = true;
                String uri = "tel:" + userEntity.getPhoneNumber().trim() ;
                Intent intent = new Intent(Intent.ACTION_DIAL );
                intent.setData(Uri.parse(uri));
                _context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return  _contacts.size();
    }


    public class MessageHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_contacts_imv_profile)
        ImageView imvPhoto;

        @BindView(R.id.item_contacts_txv_name)
        TextView txvName;

        @BindView(R.id.item_contacts_txv_phone)
        TextView txvPhone;

        @BindView(R.id.item_contacts_imv_phone)
        ImageView imvPhone;


        public MessageHolder(View view) {

            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
