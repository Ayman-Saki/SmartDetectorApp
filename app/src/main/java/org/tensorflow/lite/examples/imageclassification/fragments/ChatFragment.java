package org.tensorflow.lite.examples.imageclassification.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.tensorflow.lite.examples.imageclassification.ApiClient;
import org.tensorflow.lite.examples.imageclassification.ApiService;
import org.tensorflow.lite.examples.imageclassification.ChatAdapter;
import org.tensorflow.lite.examples.imageclassification.ChatMessage;
import org.tensorflow.lite.examples.imageclassification.ChatRequest;
import org.tensorflow.lite.examples.imageclassification.ChatResponse;
import org.tensorflow.lite.examples.imageclassification.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton;

    private ChatAdapter adapter;
    private List<ChatMessage> messages = new ArrayList<>();

    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        sendButton = view.findViewById(R.id.sendButton);

        apiService = ApiClient
                .getClient("http://192.168.1.19:8000/")
                .create(ApiService.class);

        adapter = new ChatAdapter(messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        sendButton.setOnClickListener(v -> sendMessage());

        return view;
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (text.isEmpty()) return;

        // show user message
        messages.add(new ChatMessage(text, true));
        adapter.notifyItemInserted(messages.size() - 1);

        messageInput.setText("");

        apiService.chat(new ChatRequest(text)).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.body() != null) {
                    messages.add(new ChatMessage(response.body().reply, false));
                    adapter.notifyItemInserted(messages.size() - 1);
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                messages.add(new ChatMessage("ERROR: " + t.getMessage(), false));
                adapter.notifyItemInserted(messages.size() - 1);
            }
        });
    }
}