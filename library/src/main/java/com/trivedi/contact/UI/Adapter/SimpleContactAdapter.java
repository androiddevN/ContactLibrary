package com.trivedi.contact.UI.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.trivedi.contact.UI.ContactListView;
import com.trivedi.contact.UI.Model.People;
import com.trivedi.contact.UI.R;
import com.trivedi.contact.UI.filter.ContactFilter;
import com.trivedi.contact.UI.indexer.StringArrayAlphabetIndexer;
import com.trivedi.contact.UI.ui.AvatarImageView;
import com.trivedi.contact.UI.utils.ContactImageUtil;
import com.trivedi.contact.UI.utils.ImageCache;
import com.trivedi.contact.UI.utils.LibUtils;
import com.trivedi.contact.UI.utils.async_task_thread_pool.AsyncTaskEx;
import com.trivedi.contact.UI.utils.async_task_thread_pool.AsyncTaskThreadPool;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Neeraj on 26/5/16.
 */
public class SimpleContactAdapter extends BaseAdapter implements Filterable {

    Context _mContext;
    ContactListView _mContactListView;
    ArrayList<People> peopleArrayList;
    ArrayList<People> filterPeopleArrayList;
    ArrayList<People> selectedPeopleList;
    LayoutInflater _mLayoutInflator;
    ContactFilter _mContactFilter;
    private final int CONTACT_PHOTO_IMAGE_SIZE;
    private final AsyncTaskThreadPool mAsyncTaskThreadPool = new AsyncTaskThreadPool(1, 2, 10);

    public SimpleContactAdapter(Context _mContext, ContactListView _mContactListView, ArrayList<People> peopleArrayList, ArrayList<People> selectedPeopleList, ArrayList<People> filterPeopleArrayList) {
        this._mContactListView = _mContactListView;
        this._mContext = _mContext;
        this.peopleArrayList = peopleArrayList;
        this.selectedPeopleList = selectedPeopleList;
        this.filterPeopleArrayList = filterPeopleArrayList;
        this._mLayoutInflator = LayoutInflater.from(_mContext);
        if (_mContactListView.getAvatarSize() != 0) {
            CONTACT_PHOTO_IMAGE_SIZE = LibUtils.dpToPx(_mContext, (int) _mContactListView.getAvatarSize());
        } else {
            CONTACT_PHOTO_IMAGE_SIZE = 50;
        }
    }


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
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final ViewHolder holder;
        final View rootView;

        if (convertView == null || position==0) {
            holder = new ViewHolder();
            rootView = _mLayoutInflator.inflate(R.layout.row_view, parent, false);
            holder.container = rootView;
            holder.avatar_name = (TextView) rootView.findViewById(R.id.avatar_name);
            holder.number = (TextView) rootView.findViewById(R.id.avatar_num);
            holder.selectBox = (CheckBox) rootView.findViewById(R.id.selectBox);
            holder.avatarImageView = (AvatarImageView) rootView.findViewById(R.id.avatar);
            rootView.setTag(holder);
        } else {
            rootView = convertView;
            holder = (ViewHolder) rootView.getTag();
        }
        configureLayout(holder);
        final People people = getItem(position);
        holder.avatar_name.setText(people.getName());
        holder.number.setText(people.getPhone());

        if (_mContactListView.isMultiSelectEnable()) {
            holder.selectBox.setOnCheckedChangeListener(null);
            if (selectedPeopleList.contains(people)) {
                holder.selectBox.setChecked(true);
                rootView.setBackgroundColor(Color.parseColor("#EEEEEE"));
            } else {
                holder.selectBox.setChecked(false);
                rootView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
            holder.selectBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        selectedPeopleList.add(people);
                        rootView.setBackgroundColor(Color.parseColor("#EEEEEE"));
                    } else {
                        selectedPeopleList.remove(people);
                        rootView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    }
                    _mContactListView.onCheckBoxClicked();
                }
            });
        }

        if (_mContactListView.isAvatarVisible()) {

            if (holder.updateTask != null && !holder.updateTask.isCancelled())
                holder.updateTask.cancel(true);


            boolean hasPhoto = !TextUtils.isEmpty(people.getPhotoId());
            final Bitmap cachedBitmap = hasPhoto ? ImageCache.INSTANCE.getBitmapFromMemCache(people.getPhotoId()) : null;
            if (cachedBitmap != null)
                holder.avatarImageView.setImageBitmap(cachedBitmap);
            else {
                holder.avatarImageView.setImageResource(R.drawable.default_avatar);
                if (hasPhoto) {
                    holder.updateTask = new AsyncTaskEx<Void, Void, Bitmap>() {
                        @Override
                        public Bitmap doInBackground(final Void... params) {
                            if (isCancelled())
                                return null;
                            final Bitmap b = ContactImageUtil.loadContactPhotoThumbnail(_mContext, people.getPhotoId(), CONTACT_PHOTO_IMAGE_SIZE);
                            if (b != null)
                                return ThumbnailUtils.extractThumbnail(b, CONTACT_PHOTO_IMAGE_SIZE,
                                        CONTACT_PHOTO_IMAGE_SIZE);
                            return null;
                        }

                        @Override
                        public void onPostExecute(final Bitmap result) {
                            super.onPostExecute(result);
                            if (result == null)
                                return;
                            ImageCache.INSTANCE.addBitmapToCache(people.getPhotoId(), result);
                            holder.avatarImageView.setImageBitmap(result);
                        }
                    };
                    mAsyncTaskThreadPool.executeAsyncTask(holder.updateTask);
                }
            }
        }
        return rootView;
    }

    @Override
    public Filter getFilter() {
        if (_mContactFilter == null)
            _mContactFilter = new ContactFilter(_mContext, filterPeopleArrayList, peopleArrayList, this);
        return _mContactFilter;
    }

    private static class ViewHolder {
        View container;
        TextView avatar_name, number;
        AvatarImageView avatarImageView;
        CheckBox selectBox;
        public AsyncTaskEx<Void, Void, Bitmap> updateTask;
    }

    private void configureLayout(ViewHolder viewHolder) {
        if (_mContactListView.isAvatarVisible()) {
            viewHolder.avatarImageView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.avatarImageView.setVisibility(View.GONE);
        }
        viewHolder.avatar_name.setTextSize(LibUtils.pxToDp(_mContext, (int) _mContactListView.getContactRowTxtSize()));
        viewHolder.avatar_name.setTextColor(_mContactListView.getContactRowTxtColor());
        viewHolder.container.setBackgroundColor(_mContactListView.getContactBackground());
        viewHolder.number.setTextColor(_mContext.getResources().getColor(R.color.text_color));
        if (_mContactListView.isNameOnly()) {
            viewHolder.number.setVisibility(View.GONE);
        } else {
            viewHolder.number.setVisibility(View.VISIBLE);
        }
        if (_mContactListView.isMultiSelectEnable()) {
            viewHolder.selectBox.setVisibility(View.VISIBLE);
        } else {
            viewHolder.selectBox.setVisibility(View.GONE);
        }

        if (_mContactListView.getAvatarSize() != 0) {
            LinearLayout.LayoutParams _avatarLayoutParams = new LinearLayout.LayoutParams(_mContactListView.getAvatarSize(),_mContactListView.getAvatarSize());
            viewHolder.avatarImageView.setLayoutParams(_avatarLayoutParams);
            viewHolder.avatarImageView.invalidate();
        } else {
            LinearLayout.LayoutParams _avatarLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            viewHolder.avatarImageView.setLayoutParams(_avatarLayoutParams);
            viewHolder.avatarImageView.invalidate();
        }
    }
}
