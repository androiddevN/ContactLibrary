package com.trivedi.contact;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.trivedi.contact.UI.Model.People;

import java.util.ArrayList;

/**
 * Created by Neeraj on 7/6/16.
 */
public class ContactListActivity extends AppCompatActivity {


    ListView list;
    ArrayList<People> peopleArrayList=new ArrayList<>();
    LayoutInflater layoutInflater;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        list=(ListView)findViewById(R.id.list);
        layoutInflater=LayoutInflater.from(ContactListActivity.this);
        ArrayList<People> peoples= (ArrayList<People>) getIntent().getExtras().getSerializable("people");
        if(peoples!=null){
            peopleArrayList.addAll(peoples);
        }

        list.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return peopleArrayList.size();
            }

            @Override
            public People getItem(int position) {
                return peopleArrayList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                Holder holder=null;
                if(convertView==null){
                    convertView=  layoutInflater.inflate(R.layout.row_list,null,false);
                    holder=new Holder();
                    holder.name=(TextView)convertView.findViewById(R.id.name);
                    convertView.setTag(holder);
                }

                holder= (Holder) convertView.getTag();
                holder.name.setText(getItem(position).getName());
                return convertView;
            }

            class Holder{
                TextView name;
            }

        });

    }
}
