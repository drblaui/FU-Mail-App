package me.drblau.fumailapp.ui.spam;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SpamViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public SpamViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Spam Fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
