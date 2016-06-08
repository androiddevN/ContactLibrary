package com.trivedi.contact.UI.filter;

import android.content.Context;
import android.widget.BaseAdapter;
import android.widget.Filter;

import com.trivedi.contact.UI.Adapter.SimpleContactAdapter;
import com.trivedi.contact.UI.Model.People;

import java.util.ArrayList;

/**
 * Created by Neeraj on 7/6/16.
 */
public class ContactFilter extends Filter {

    Context _mContext;
    ArrayList<People> filterPeopleArrayList;
    BaseAdapter adapter;
    ArrayList<People> peopleArrayList;


    public ContactFilter(Context _mContext,ArrayList<People> filterPeopleArrayList,ArrayList<People> peopleArrayList,BaseAdapter adapter){
        this._mContext=_mContext;
        this.filterPeopleArrayList=filterPeopleArrayList;
        this.peopleArrayList=peopleArrayList;
        this.adapter=adapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults _mFilterResult = new FilterResults();
        constraint = constraint.toString().toLowerCase().trim();
        ArrayList<People> localList=new ArrayList<>();
        if(constraint!=null && constraint.length()>0){
            for(int i=0;i<filterPeopleArrayList.size();i++){
             if(filterPeopleArrayList.get(i).getName().toLowerCase().startsWith(constraint.toString()))
                 localList.add(filterPeopleArrayList.get(i));
            }
            _mFilterResult.count = localList.size();
            _mFilterResult.values = localList;
        }else{
            _mFilterResult.count = filterPeopleArrayList.size();
            _mFilterResult.values = filterPeopleArrayList;
        }
        return _mFilterResult;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        ArrayList<People> resultPeopleList = (ArrayList<People>) results.values;
        peopleArrayList.clear();
        if(resultPeopleList!=null){
            peopleArrayList.addAll(resultPeopleList);
        }
        if(adapter instanceof SimpleContactAdapter){
            adapter.notifyDataSetChanged();
        }
    }
}
