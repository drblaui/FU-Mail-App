package me.drblau.fumailapp.ui.inbox;

import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class InboxViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public InboxViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Inbox Fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
