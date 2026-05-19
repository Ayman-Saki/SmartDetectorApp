package org.tensorflow.lite.examples.imageclassification;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int USER = 1;
    private static final int BOT = 2;

    private List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser ? USER : BOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == USER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_message, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_bot_message, parent, false);
            return new BotViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ChatMessage message = messages.get(position);

        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).text.setText(message.message);
        } else {
            ((BotViewHolder) holder).text.setText(message.message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView text;

        UserViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.messageText);
        }
    }

    static class BotViewHolder extends RecyclerView.ViewHolder {
        TextView text;

        BotViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.messageText);
        }
    }
}