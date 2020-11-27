package me.drblau.fumailapp.ui.unread;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UnreadViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public UnreadViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
