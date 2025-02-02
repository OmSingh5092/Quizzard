package com.andronauts.quizzard.general.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import com.andronauts.quizzard.R;
import com.andronauts.quizzard.api.responseModels.faculty.FacultyGetListResponse;
import com.andronauts.quizzard.api.retrofit.RetrofitClient;
import com.andronauts.quizzard.dataModels.Faculty;
import com.andronauts.quizzard.databinding.ActivityFacultiesBinding;
import com.andronauts.quizzard.general.adapters.FacultyProfileRecycler;
import com.andronauts.quizzard.utils.SharedPrefs;

import java.util.ArrayList;
import java.util.List;

public class FacultiesActivity extends AppCompatActivity {
    private ActivityFacultiesBinding binding;
    private SharedPrefs prefs;
    private FacultyProfileRecycler adapter;
    private List<Faculty> faculties;
    private List<Faculty> sortedFaculties;
    private boolean isStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFacultiesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        prefs = new SharedPrefs(this);
        isStudent = getIntent().getBooleanExtra("isStudent",false);
        if(isStudent){
            binding.toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        setSupportActionBar(binding.toolbar);

        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        binding.search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                sortList(editable.toString());
            }
        });

        loadData();
    }

    private void loadData(){
        RetrofitClient.getClient().facultyGetAll(prefs.getToken()).enqueue(new Callback<FacultyGetListResponse>() {
            @Override
            public void onResponse(Call<FacultyGetListResponse> call, Response<FacultyGetListResponse> response) {
                if(response.isSuccessful()){
                    faculties = response.body().getFaculties();
                    sortedFaculties = new ArrayList<>(faculties);
                    setUpRecyclerView();
                }
            }

            @Override
            public void onFailure(Call<FacultyGetListResponse> call, Throwable t) {

            }
        });
    }

    private void sortList(String key){
        sortedFaculties.clear();
        for(Faculty faculty:faculties){
            if(faculty.getName().toLowerCase().contains(key) ||
                faculty.getFacultyId().toLowerCase().contains(key)){
                sortedFaculties.add(faculty);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void setUpRecyclerView(){
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FacultyProfileRecycler(this,sortedFaculties,isStudent);
        binding.recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}