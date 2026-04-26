package com.ensab.reservaapp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProfileViewModel extends ViewModel {
    public final MutableLiveData<String> name = new MutableLiveData<>("");
    public final MutableLiveData<String> email = new MutableLiveData<>("");
    public final MutableLiveData<String> phone = new MutableLiveData<>("");
    public final MutableLiveData<String> profileImageUrl = new MutableLiveData<>("");

    // Indicateur pour savoir si les données ont déjà été chargées depuis Firebase
    private boolean dataLoaded = false;

    public boolean isDataLoaded() {
        return dataLoaded;
    }

    public void setDataLoaded(boolean loaded) {
        this.dataLoaded = loaded;
    }
}
