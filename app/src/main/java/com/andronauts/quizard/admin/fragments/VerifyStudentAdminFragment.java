package com.andronauts.quizard.admin.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andronauts.quizard.databinding.FragmentVerifyStudentAdminBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class VerifyStudentAdminFragment extends Fragment {
    private FragmentVerifyStudentAdminBinding binding;
    private Context context;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentVerifyStudentAdminBinding.inflate(getLayoutInflater(),container,false);
        context = getContext();

        return binding.getRoot();
    }
}
