package me.drblau.fumailapp.ui.empty;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EmptyViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public EmptyViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Please Login");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
