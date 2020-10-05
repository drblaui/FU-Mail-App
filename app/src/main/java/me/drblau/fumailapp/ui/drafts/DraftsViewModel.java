package me.drblau.fumailapp.ui.drafts;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DraftsViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public DraftsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is drafts Fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
