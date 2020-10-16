package com.andronauts.quizard.api.responseModels.student;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StudentSubjectDeleteResponse {
    @SerializedName("success")
    @Expose
    private boolean success;
    @SerializedName("msg")
    @Expose
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
