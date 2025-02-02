package com.andronauts.quizzard.general.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.andronauts.quizzard.R;
import com.andronauts.quizzard.api.WebSocketClient;
import com.andronauts.quizzard.api.responseModels.chat.ChatGetListResponse;
import com.andronauts.quizzard.api.responseModels.faculty.FacultyGetResponse;
import com.andronauts.quizzard.api.responseModels.student.StudentGetResponse;
import com.andronauts.quizzard.api.retrofit.RetrofitClient;
import com.andronauts.quizzard.dataModels.Chat;
import com.andronauts.quizzard.dataModels.Faculty;
import com.andronauts.quizzard.dataModels.Student;
import com.andronauts.quizzard.databinding.ActivityChatBinding;
import com.andronauts.quizzard.general.adapters.ChatRecycler;
import com.andronauts.quizzard.utils.SharedPrefs;
import com.github.nkzawa.emitter.Emitter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;
    private SharedPrefs prefs;
    private String receiverId;
    private List<Chat> chats;
    private ChatRecycler adapter;
    private Chat chat;
    private Student student;
    private Faculty faculty;
    private String senderId;
    private boolean isSenderStudent;
    private boolean isReceiverStudent;
    private String name;

    private int MEDIA_REQUEST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        receiverId = getIntent().getStringExtra("receiverId");
        isSenderStudent = getIntent().getBooleanExtra("isSenderStudent",false);
        isReceiverStudent = getIntent().getBooleanExtra("isReceiverStudent",false);
        name = getIntent().getStringExtra("name");
        binding.toolbar.setTitle(name);

        if(isSenderStudent){
            binding.toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            setSupportActionBar(binding.toolbar);
        }


        prefs = new SharedPrefs(this);
        chat = new Chat();

        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
        loadSenderProfile();
        loadReceiverProfile();
        setSendMessage();
    }

    private void loadReceiverProfile(){

    }

    private void loadSenderProfile(){
        if(isSenderStudent){
            RetrofitClient.getClient().studentGetProfile(prefs.getToken()).enqueue(new Callback<StudentGetResponse>() {
                @Override
                public void onResponse(Call<StudentGetResponse> call, Response<StudentGetResponse> response) {
                    if(response.isSuccessful()){
                        student = response.body().getStudent();
                        senderId = student.getId();
                        loadData();
                    }
                }

                @Override
                public void onFailure(Call<StudentGetResponse> call, Throwable t) {

                }
            });
        }else{
            RetrofitClient.getClient().facultyGetProfile(prefs.getToken()).enqueue(new Callback<FacultyGetResponse>() {
                @Override
                public void onResponse(Call<FacultyGetResponse> call, Response<FacultyGetResponse> response) {
                    if(response.isSuccessful()){
                        faculty = response.body().getFaculty();
                        senderId = faculty.getId();
                        loadData();
                    }
                }

                @Override
                public void onFailure(Call<FacultyGetResponse> call, Throwable t) {

                }
            });
        }
    }

    private void loadData(){
        RetrofitClient.getClient().getAllChats(prefs.getToken(),receiverId).enqueue(new Callback<ChatGetListResponse>() {
            @Override
            public void onResponse(Call<ChatGetListResponse> call, Response<ChatGetListResponse> response) {
                if(response.isSuccessful()){
                    chats = response.body().getChats();
                    setUpRecyclerView();
                    setWebSocket();
                }

                binding.swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ChatGetListResponse> call, Throwable t) {

            }
        });
    }

    private void setUpRecyclerView(){
        LinearLayoutManager manager = new LinearLayoutManager(this, RecyclerView.VERTICAL,false);
        manager.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(manager);
        adapter = new ChatRecycler(this,chats,senderId);
        binding.recyclerView.setAdapter(adapter);
    }

    private void setSendMessage(){

        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chat.setMessage(binding.message.getText().toString());
                chat.setSender(senderId);
                chat.setReceiver(receiverId);
                chat.setTime(String.valueOf(System.currentTimeMillis()));
                try {
                    JSONObject object = new JSONObject(new Gson().toJson(chat));
                    WebSocketClient.getMSocket().emit("receive_chat",object);
                    chats.add(chat);
                    chat = new Chat();
                    setUpRecyclerView();
                    binding.message.setText("");
                    binding.delete.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        binding.media.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
                pictureIntent.setType("image/*");
                pictureIntent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(pictureIntent,"Select Picture"), MEDIA_REQUEST);
            }
        });

        binding.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chat.setMedia(null);
                binding.delete.setVisibility(View.GONE);
            }
        });
    }

    private void handlePhotoUpload(Uri uri){
        Snackbar snackbar = Snackbar.make(binding.getRoot(),"Uploading...",Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
        String path = "chats/"+prefs.getEmail()+"/"+System.currentTimeMillis()+".jpg";
        FirebaseStorage.getInstance().getReference(path).putFile(uri)
            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    snackbar.dismiss();
                    Toast.makeText(ChatActivity.this, "Upload Successful!", Toast.LENGTH_SHORT).show();
                    chat.setMedia(path);

                    binding.delete.setVisibility(View.VISIBLE);
                }
            });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MEDIA_REQUEST){
            handlePhotoUpload(data.getData());
        }
    }

    private void setWebSocket(){
        WebSocketClient.getMSocket().on("send_chat/" + senderId, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                ChatActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject object = (JSONObject) args[0];
                        Chat chat = new Gson().fromJson(object.toString(),Chat.class);
                        chats.add(chat);
                        setUpRecyclerView();
                    }
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}