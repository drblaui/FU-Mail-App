package me.drblau.fumailapp.ui.sent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SentViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public SentViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Sent Fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
