package com.trivedi.contact.UI.listener;

import com.trivedi.contact.UI.Model.People;

import java.util.ArrayList;

/**
 * Created by Neeraj on 26/5/16.
 */
public interface DataCallback {
    public void onPeopleData(ArrayList<People> peopleArrayList,boolean isError);
}
