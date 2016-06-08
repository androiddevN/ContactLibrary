package com.trivedi.contact.UI.Task;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;

import com.trivedi.contact.UI.ContactListView;
import com.trivedi.contact.UI.Model.People;
import com.trivedi.contact.UI.utils.ContactsQuery;

import java.util.ArrayList;

/**
 * Created by Neeraj on 26/5/16.
 */
public class ContactThreadSync  extends AsyncTask<Void, Integer, ArrayList<People>> {

    Context _mContext;              // Context instance
    ContactListView _mContactList;  // Pass ContactListview instance to bind listener for sending data
    boolean isNameOnly=false;
    ProgressDialog _mProgressDialog;


   public ContactThreadSync(Context context,ContactListView _mContactList){
        this._mContext=context;
        this._mContactList=_mContactList;
        isNameOnly=_mContactList.isNameOnly();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        _mContactList.showLoaderView();
    }

    @Override
    protected ArrayList doInBackground(Void... params) {
        ArrayList<People> phoneContact = new ArrayList<>();
        Uri uri = ContactsQuery.CONTENT_URI;
        int _mCount=0;
        ContentResolver cr = _mContext.getContentResolver();
        final Cursor cur = cr.query(uri, ContactsQuery.PROJECTION, ContactsQuery.SELECTION, null, ContactsQuery.SORT_ORDER);
        if (cur == null) {
            return null;
        }else{
           final int count= cur.getCount();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        _mProgressDialog= _mContactList.getProgressBarDialog();
                        _mProgressDialog.setMax(count);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                People people = new People();
                String id = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts._ID));
                Uri contactUri= ContactsContract.Contacts.getLookupUri(
                        cur.getLong(ContactsQuery.ID),
                        cur.getString(ContactsQuery.LOOKUP_KEY));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));
                String photoId=cur.getString(ContactsQuery.PHOTO_THUMBNAIL_DATA);
                people.setId(String.valueOf(id));
                people.setContactUri(contactUri.toString());
                people.setName(name);
                people.setPhotoId(photoId);

               if(!isNameOnly) {
                   if (Integer.parseInt(cur.getString(cur.getColumnIndex(
                           ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                       Cursor pCur = cr.query(
                               ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                               null,
                               ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                               new String[]{id}, null);
                       while (pCur.moveToNext()) {
                           String phoneNumber = pCur.getString(pCur.getColumnIndex(
                                   ContactsContract.CommonDataKinds.Phone.NUMBER));
                           if (phoneNumber != null) {
                               String phoneNumber1 = phoneNumber.replaceAll("\\s+", "").replaceAll("\\([^\\(]*\\)", "").replaceAll("-", "");
                               if (phoneNumber1.trim().length() > 0) {
                                   if (phoneNumber1.charAt(0) == '0') {
                                       phoneNumber1 = "+91" + phoneNumber1.substring(1);
                                   } else if (phoneNumber1.charAt(0) == '+') {

                                   } else {
                                       phoneNumber1 = "+91" + phoneNumber1;
                                   }
                               }
                               people.setPhone(phoneNumber1);
                           }
                       }
                       pCur.close();
                   }
               }
                publishProgress(++_mCount);
                phoneContact.add(people);
            }
        }
        return phoneContact;
    }

    @Override
    protected void onPostExecute(ArrayList<People> result) {
        _mContactList.hideLoaderView();
        if (result != null) {
          _mContactList.onPeopleData(result,false);
        }else{
           _mContactList.onPeopleData(null,true);
        }
    }


    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        _mProgressDialog.incrementProgressBy(1);
    }
}
