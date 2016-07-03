package com.trivedi.contact;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.trivedi.contact.UI.ContactListView;
import com.trivedi.contact.UI.Model.People;
import com.trivedi.contact.UI.listener.OnPeopleCheckCountListener;
import com.trivedi.contact.UI.listener.OnSelectedPeopleList;

import java.util.ArrayList;

/**
 * Created by Neeraj on 26/5/16.
 */
public class MainActivity extends AppCompatActivity implements OnSelectedPeopleList, OnPeopleCheckCountListener {

    ContactListView _mContactList;
    MenuItem doneBtn;
    MenuItem searchMenu;
    MenuItem multipleMenu;
    MenuItem selectAllbtn;
    MenuItem searchEnable;
    MenuItem indexEnable;
    SearchView searchView;
    boolean isMenuSearchEnable = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        getSupportActionBar().setTitle(R.string.contacts);
        _mContactList = (ContactListView) findViewById(R.id.contactList);
        _mContactList.setOnSelectedPeopleListener(this);
        _mContactList.setOnPeopleCheckCountListener(this);
        _mContactList.setIsAvatarVisible(true)
                .setIsNameOnly(false)
                .setProgressShow(true)
                .setIsMultiSelectEnable(false)
                .setIsCacheEnable(false)
                .setContainerBackground("#205A7D8C")
                .setDividerHeight(0)
                .setContactRowTxtColor(Color.parseColor("#323232"))
                .setIsIndexable(false)
                .buildView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        doneBtn = menu.findItem(R.id.doneBtn);
        searchMenu = menu.findItem(R.id.searchMenu);
        multipleMenu = menu.findItem(R.id.multipleMenu);
        selectAllbtn = menu.findItem(R.id.selectAllbtn);
        searchEnable = menu.findItem(R.id.searchenable);
        indexEnable = menu.findItem(R.id.indexenable);
        doneBtn.setVisible(false);
        selectAllbtn.setVisible(false);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.searchMenu));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String query) {
                _mContactList.performSearchQuery(query);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.doneBtn:
                _mContactList.performDone();
                return true;
            case R.id.multipleMenu:
                if (item.isChecked()) {
                    item.setChecked(false);
                    selectAllbtn.setVisible(false);
                    _mContactList.setIsMultiSelectEnable(false);
                    doneBtn.setVisible(false);
                } else {
                    _mContactList.setIsMultiSelectEnable(true);
                    item.setChecked(true);
                    selectAllbtn.setVisible(true);
                    doneBtn.setVisible(true);
                }
                return true;
            case R.id.searchenable:
                if (!isMenuSearchEnable) {
                    item.setTitle(R.string.disable_search);
                    searchMenu.setVisible(true);
                    isMenuSearchEnable = true;
                } else {
                    item.setTitle(R.string.enable_search);
                    searchMenu.setVisible(false);
                    isMenuSearchEnable = false;
                }
                return true;
            case R.id.selectAllbtn:
                if (!_mContactList.getSelectAllStatus()) {
                    item.setTitle(R.string.unselect_all);
                    _mContactList.performSelectAll(true);
                } else {
                    item.setTitle(R.string.select_all);
                    _mContactList.performSelectAll(false);
                }
                return true;

            case R.id.indexenable:
                if (!_mContactList.isIndexable()) {
                    _mContactList.setIsIndexable(true);
                    item.setTitle(R.string.normal_list);
                } else {
                    _mContactList.setIsIndexable(false);
                    item.setTitle(R.string.index_list);
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void getSelectedPeopleList(ArrayList<People> peopleArrayList) {
        if (peopleArrayList != null) {
            Intent listIntent = new Intent(MainActivity.this, ContactListActivity.class);
            listIntent.putExtra("people", peopleArrayList);
            startActivity(listIntent);
        }
    }

    @Override
    public void selectedPeopleCount(int _mSelectedCount) {
        if (_mSelectedCount > 0) {
            getSupportActionBar().setTitle(_mSelectedCount + getString(R.string.selected));
        } else {
            getSupportActionBar().setTitle(R.string.contacts);
        }

    }
}
