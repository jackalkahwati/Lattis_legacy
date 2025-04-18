package com.lattis.ellipse.presentation.ui.payment;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.lattis.ellipse.domain.model.Card;
import com.lattis.ellipse.presentation.ui.bike.BikeInfoActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.lattis.ellipse.R;

public class UserCardListAdapter extends RecyclerView.Adapter<UserCardListAdapter.ViewHolder> {
    List<Card> cardList;
    Context mContext;
    boolean isSetClickEvent;
    UserCardListListener userCardListListener;


    public UserCardListAdapter(Context context, List<Card> cards, boolean isShowCheckbox, UserCardListListener listener) {
        this.mContext = context;
        this.cardList = cards;
        this.isSetClickEvent = isShowCheckbox;
        this.userCardListListener = listener;

    }


    @Override
    public UserCardListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_card_view, parent, false);
        return new UserCardListAdapter.ViewHolder(view, cardList, isSetClickEvent);
    }

    @Override
    public void onBindViewHolder(UserCardListAdapter.ViewHolder holder, int position) {
        if (cardList != null) {
            holder.tv_cardType.setText(cardList.get(position).getCc_type());
            String number = cardList.get(position).getCc_no();
            number = number.substring(number.length() - 8);
            number = number.substring(0, number.length() - 4) + "\n" + number.substring(number.length() - 4, number.length()); // 18:00
            holder.tv_cardNumber.setText(number);
            String cardName = cardList.get(position).getCc_type().toUpperCase().replace(" ", "_");
            holder.card_number_icon.setImageResource(getResource(cardName));
            holder.checkbox.setChecked(cardList.get(position).getIs_primary());
            holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        userCardListListener.onclickCheckBox(position);
                    } else {
                        holder.checkbox.setChecked(true);
                    }

                }
            });

        }


    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.tv_cardType)
        TextView tv_cardType;
        @BindView(R.id.tv_cardNumber)
        TextView tv_cardNumber;
        @BindView(R.id.checkbox)
        CheckBox checkbox;
        @BindView(R.id.card_number_icon)
        ImageView card_number_icon;
        List<Card> list;

        public ViewHolder(View itemView, List<Card> cardList, boolean isSetClickEvent) {
            super(itemView);
            if (isSetClickEvent)
                itemView.setOnClickListener(this);
            this.list = cardList;
            ButterKnife.bind(this, itemView);
        }


        @Override
        public void onClick(View v) {
            v.getContext().startActivity(new Intent(v.getContext()
                    , AddCardActivity.class).putExtra("CARD_DETAILS",
                    new Gson().toJson(list.get(getAdapterPosition()))));
        }
    }

    private int getResource(String card_type) {
        switch (card_type) {
            case "VISA":
                return R.drawable.bt_ic_visa;

            case "MASTERCARD":
                return R.drawable.bt_ic_mastercard;

            case "DISCOVER":
                return R.drawable.bt_ic_discover;

            case "AMERICAN_EXPRESS":
                return R.drawable.bt_ic_amex;

            case "MAESTRO":
                return R.drawable.bt_ic_maestro;

            case "DINERS_CLUB":
                return R.drawable.bt_ic_diners_club;

            case "UNIONPAY":
                return R.drawable.bt_ic_unionpay;

            case "JCB":
                return R.drawable.bt_ic_jcb;
        }

        return R.drawable.bt_ic_unknown;
    }

}
