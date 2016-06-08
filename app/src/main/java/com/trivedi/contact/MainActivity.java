package com.trivedi.contact;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.trivedi.contact.UI.ContactListView;
import com.trivedi.contact.UI.Model.People;
import com.trivedi.contact.UI.listener.OnSelectedPeopleList;

import java.util.ArrayList;

/**
 * Created by Neeraj on 26/5/16.
 */
public class MainActivity extends AppCompatActivity implements OnSelectedPeopleList {


    ContactListView _mContactList;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        _mContactList=(ContactListView)findViewById(R.id.contactList);
        _mContactList.setOnSelectedPeopleListener(this);

    }

    @Override
    public void getSelectedPeopleList(ArrayList<People> peopleArrayList) {

        if(peopleArrayList!=null)
        {
            Intent listIntent=new Intent(MainActivity.this,ContactListActivity.class);
            listIntent.putExtra("people",peopleArrayList);
            startActivity(listIntent);
        }
    }
}
