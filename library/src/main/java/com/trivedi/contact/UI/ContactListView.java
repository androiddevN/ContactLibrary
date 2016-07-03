package com.trivedi.contact.UI;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.trivedi.contact.UI.Adapter.SimpleContactAdapter;
import com.trivedi.contact.UI.Adapter.SimpleIndexContactAdapter;
import com.trivedi.contact.UI.Model.People;
import com.trivedi.contact.UI.Task.ContactThreadSync;
import com.trivedi.contact.UI.cache.ContactCache;
import com.trivedi.contact.UI.listener.DataCallback;
import com.trivedi.contact.UI.listener.OnCheckedListener;
import com.trivedi.contact.UI.listener.OnPeopleCheckCountListener;
import com.trivedi.contact.UI.listener.OnSelectedPeopleList;
import com.trivedi.contact.UI.ui.PinnedHeaderListView;
import com.trivedi.contact.UI.utils.InitiateSearch;
import com.trivedi.contact.UI.utils.LibUtils;

import java.util.ArrayList;


//A highly customized library which fetch all the phone contacts and display them nicely in a listview

/**
 * Created by Neeraj on 26/5/16.
 */
public class ContactListView extends FrameLayout implements DataCallback, OnCheckedListener,OnItemClickListener {

    Context mContext;
    /**
     * List View to inflate Contact list
     */
    private ListView _mListview;

    private PinnedHeaderListView _mPinnedHeaderListView;

    private LinearLayout _mContainerView;

    private boolean _mHeadershow = false;

    private boolean _mLoadershow = true;

    private boolean _mProgressShow = false;

    private int _mContactBackground;

    private int _mContactDivider;

    private int _mContactRowTxtColor;

    private float _mDividerHeight = 1.0f;      // set Default value 5.0

    private float _mContactRowTxtSize = 20.0f;

    private boolean isAvatarVisible = false;

    private boolean isNameOnly = true;

    private boolean isSearchEnable = false;

    private boolean isMultiSelectEnable = false;

    private boolean isIndexable = false;

    private int _mHeaderBgColor;

    private int _mBackground;

    private int _mAvatarSize = 60;

    private boolean isCacheEnable = false;

    private TextView _mErrorText;

    private ArrayList<People> _mPeopleList = new ArrayList<>();

    private ArrayList<People> _mSelectedPeopleList = new ArrayList<>();

    private ArrayList<People> filterPeopleArrayList = new ArrayList<>();

    private SimpleContactAdapter _mSimpleAdapter;

    private SimpleIndexContactAdapter _mSimpleIndexAdapter;

    private LayoutInflater _mLayoutInflater;

    /**
     * Header view like Action bar view to show selected contact count and done button.
     */
    View _headerView;

    View _noContactView;

    OnSelectedPeopleList selectedListener;

    OnPeopleCheckCountListener _mPeopleCheckCountListener;

    public boolean selectFlag = false;           // flag to use select all/Unselect all

    ProgressDialog _mProgressDailog;

    /**
     * Header View Component
     */
    EditText _mEditSearchView;
    ImageButton _mSearchButton;
    ImageButton _mDoneButton;
    ImageButton _mSelectButton;
    ImageView _mClearSearch;
    CardView _SearchCardView;
    RelativeLayout _mSearchButtonContainer;
    ImageView _mSearchBack;
    TextView mTitleName;


    private static final int READ_CONTACTS_PERMISSIONS_REQUEST = 1;


    public ContactListView(Context context) {
        this(context, null);
    }

    public ContactListView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.ContactStyle);
    }

    public ContactListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        final TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ContactListView, defStyleAttr, 0);
        _mContactBackground = attributes.getColor(R.styleable.ContactListView_contact_row_background, getResources().getColor(R.color.WHITE));
        _mContactDivider = attributes.getColor(R.styleable.ContactListView_contact_row_divider_color, getResources().getColor(R.color.BLACK));
        _mContactRowTxtColor = attributes.getColor(R.styleable.ContactListView_contact_row_text_color, getResources().getColor(R.color.BLACK));
        _mContactRowTxtSize = attributes.getDimension(R.styleable.ContactListView_contact_row_text_size, LibUtils.dpToPx(mContext, 16));
        _mAvatarSize = attributes.getDimensionPixelSize(R.styleable.ContactListView_avatar_size, LibUtils.dpToPx(mContext, 48));
        _mHeadershow = attributes.getBoolean(R.styleable.ContactListView_show_header, false);
        isAvatarVisible = attributes.getBoolean(R.styleable.ContactListView_show_avatar, false);
        isNameOnly = attributes.getBoolean(R.styleable.ContactListView_show_name_only, false);
        isSearchEnable = attributes.getBoolean(R.styleable.ContactListView_search_enable, false);
        isCacheEnable = attributes.getBoolean(R.styleable.ContactListView_cache_enable, false);
        isMultiSelectEnable = attributes.getBoolean(R.styleable.ContactListView_multiselect_enable, false);
        isIndexable = attributes.getBoolean(R.styleable.ContactListView_indexableview, false);
        _mProgressShow = attributes.getBoolean(R.styleable.ContactListView_show_loading_progress, false);
        _mDividerHeight = attributes.getDimension(R.styleable.ContactListView_row_divider_height, 1.0f);
        _mBackground = attributes.getColor(R.styleable.ContactListView_contact_background, getResources().getColor(R.color.WHITE));
        _mHeaderBgColor = attributes.getColor(R.styleable.ContactListView_header_bg_color, getResources().getColor(R.color.primary));
        _mLayoutInflater = LayoutInflater.from(context);

    }


    public void buildView() {
        init();   // Initilize View Container
    }

    /**
     * Initlize view and attribute
     */
    private void init() {
        removeAllViews();  // Remove all view first to clear all child view
        LayoutParams _mContainerParams = new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        _mContainerView = new LinearLayout(mContext);
        _mContainerView.setLayoutParams(_mContainerParams);
        _mContainerView.setWeightSum(1);
        _mContainerView.setOrientation(LinearLayout.VERTICAL);
        _mContainerView.setBackgroundColor(_mBackground);
        this.addView(_mContainerView);
        if (isHeadershow()) {
            headerViewInitilize();
        }
        configContactListview();
        configErrorMessageView();
        configNoContactExistView();
        manageContactCache();

    }


    private void configErrorMessageView() {
        LayoutParams _mErrorContainer = new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        _mErrorText = new TextView(mContext);
        _mErrorText.setPadding(30, 30, 30, 30);
        _mErrorText.setTextColor(getResources().getColor(R.color.WHITE));
        _mErrorText.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        _mErrorText.setLayoutParams(_mErrorContainer);
        _mErrorText.setText(R.string.please_select_people_to_proceed);
        _mErrorText.setTypeface(null, Typeface.BOLD_ITALIC);
        _mErrorText.setGravity(Gravity.CENTER);
        _mErrorText.setVisibility(View.GONE);
        this.addView(_mErrorText);
    }


    private void configNoContactExistView() {
        _noContactView = _mLayoutInflater.inflate(R.layout.contact_not_found_view, null, false);
        LayoutParams _noContactLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        _noContactView.setLayoutParams(_noContactLayoutParams);
        this.addView(_noContactView);
        _noContactView.setVisibility(View.GONE);
    }


    /**
     * Manage Contact cache so that it will not fetch again if once fetched
     * It also depend upon setting if user want cache
     */
    private void manageContactCache() {
        ContactCache _mCache = ContactCache.getInstance();
        if (_mCache.getContactCacheObject(mContext) == null || !isCacheEnable) {
                 new ContactThreadSync(mContext, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        } else {
            ArrayList<People> _mCachePeopleList = _mCache.getContactCacheObject(mContext);
            if (_mCachePeopleList.size() > 0) {
                _mPeopleList.clear();
                _mPeopleList.addAll(_mCachePeopleList);
                filterPeopleArrayList.addAll(_mPeopleList);

                if (_mSimpleAdapter != null)
                    _mSimpleAdapter.notifyDataSetChanged();

                if (_mSimpleIndexAdapter != null)
                    _mSimpleIndexAdapter.notifyDataSetChanged();

            }

        }
    }


    private void cacheContactPeopleData(ArrayList<People> _mPeopleList) {
        ContactCache _mCache = ContactCache.getInstance();
        _mCache.makeContactCacheSpace(mContext, _mPeopleList);

    }


    /**
     * Header View Initilize in container
     */
    private void headerViewInitilize() {
        final InitiateSearch initiateSearch = new InitiateSearch();
        _headerView = _mLayoutInflater.inflate(R.layout.header_toolbar_view, null, false);
        mTitleName = (TextView) _headerView.findViewById(R.id.title_template);
        _mEditSearchView = (EditText) _headerView.findViewById(R.id.edit_text_search);
        _mSearchButton = (ImageButton) _headerView.findViewById(R.id.searchbtn);
        _mDoneButton = (ImageButton) _headerView.findViewById(R.id.doneBtn);
        _mSelectButton = (ImageButton) _headerView.findViewById(R.id.selectAllbtn);
        _mClearSearch = (ImageView) _headerView.findViewById(R.id.clearSearch);
        _SearchCardView = (CardView) _headerView.findViewById(R.id.card_search);
        _mSearchButtonContainer = (RelativeLayout) _headerView.findViewById(R.id.view_search);
        _mSearchBack = (ImageView) _headerView.findViewById(R.id.image_search_back);
        _mSearchButtonContainer.setBackgroundColor(_mHeaderBgColor);
        if (!isSearchEnable()) {
            _SearchCardView.setVisibility(View.GONE);
            _mSearchButton.setVisibility(View.GONE);
        }

        if (!isMultiSelectEnable()) {
            _mDoneButton.setVisibility(View.GONE);
            _mSelectButton.setVisibility(View.GONE);
        } else {
            _mDoneButton.setVisibility(View.VISIBLE);
            _mSelectButton.setVisibility(View.VISIBLE);

            /**
             *  Done to send data to implemented listener if any
             */
            _mDoneButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedListener != null) {
                        if (_mSelectedPeopleList.size() > 0) {
                            selectedListener.getSelectedPeopleList(_mSelectedPeopleList);
                        } else {
                            showErrorMessage(mContext.getString(R.string.please_select_people_to_proceed));
                        }
                    }
                }
            });

            /**
             *  Select All People in one Go
             */
            _mSelectButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    _mSelectedPeopleList.clear();
                    if (!selectFlag) {
                        selectFlag = true;
                        _mSelectedPeopleList.addAll(_mPeopleList);
                    } else {
                        selectFlag = false;
                    }
                    if (_mSimpleAdapter != null)
                        _mSimpleAdapter.notifyDataSetChanged();
                    onCheckBoxClicked();
                }
            });
        }

        _mContainerView.addView(_headerView);

        _mSearchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                initiateSearch.handleToolBar(mContext, _SearchCardView, _mSearchButtonContainer, _mEditSearchView, _mSearchButton);
            }
        });
        _mSearchBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                initiateSearch.handleToolBar(mContext, _SearchCardView, _mSearchButtonContainer, _mEditSearchView, _mSearchButton);
            }
        });

        _mEditSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (_mEditSearchView.getText().toString().length() == 0) {
                    _mClearSearch.setImageResource(R.drawable.ic_keyboard_voice);
                } else {
                    _mClearSearch.setImageResource(R.drawable.ic_close);
                }
                _mSimpleAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        _mClearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _mEditSearchView.setText("");
                _mPeopleList.clear();
                _mPeopleList.addAll(filterPeopleArrayList);
                _mSimpleAdapter.notifyDataSetChanged();
                ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });
    }


    private void showProgressBarDialog() {
        _mProgressDailog = new ProgressDialog(mContext);
        _mProgressDailog.setMessage(mContext.getString(R.string.please_wait_message));
        _mProgressDailog.setTitle(mContext.getString(R.string.fetching_contact_title));
        _mProgressDailog.setIndeterminate(false);
        if (_mProgressShow) {
            _mProgressDailog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        } else {
            _mProgressDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        _mProgressDailog.setCancelable(false);
        _mProgressDailog.setCanceledOnTouchOutside(false);
    }


    public ProgressDialog getProgressBarDialog() {
        if (_mProgressDailog == null) {
            showProgressBarDialog();
        }
        return _mProgressDailog;
    }


    public boolean isProgressShow() {
        return _mProgressShow;
    }

    public ContactListView setProgressShow(boolean _mProgressShow) {

        this._mProgressShow = _mProgressShow;
        if (_mProgressDailog != null) {
            if (_mProgressShow) {
                _mProgressDailog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            } else {
                _mProgressDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            }
        }
        invalidate();
        return this;
    }

    public int getAvatarSize() {
        return _mAvatarSize;
    }

    public ContactListView setAvatarSize(int _mAvatarSize) {
        this._mAvatarSize = _mAvatarSize;
        return this;
    }


    public boolean isMultiSelectEnable() {
        return isMultiSelectEnable;
    }

    public ContactListView setIsMultiSelectEnable(boolean isMultiSelectEnable) {

        this.isMultiSelectEnable = isMultiSelectEnable;
        if(!isMultiSelectEnable){
            _mSelectedPeopleList.clear();
            onCheckBoxClicked();
        }

        if(_mSimpleAdapter!=null)
            _mSimpleAdapter.notifyDataSetChanged();

        if(_mSimpleIndexAdapter!=null)
            _mSimpleIndexAdapter.notifyDataSetChanged();
        return this;
    }

    public boolean isSearchEnable() {
        return isSearchEnable;
    }

    public ContactListView setIsSearchEnable(boolean isSearchEnable) {
        this.isSearchEnable = isSearchEnable;
        return this;
    }

    public boolean isNameOnly() {
        return isNameOnly;
    }

    public ContactListView setIsNameOnly(boolean isNameOnly) {
        this.isNameOnly = isNameOnly;
        return this;
    }

    public boolean isHeadershow() {
        return _mHeadershow;
    }

    public ContactListView setHeaderEnable(boolean _mHeadershow) {
        this._mHeadershow = _mHeadershow;
        return this;
    }

    public boolean isLoadershow() {
        return _mLoadershow;
    }

    public void setLoaderEnable(boolean _mLoadershow) {
        this._mLoadershow = _mLoadershow;
    }


    public int getContainerBackground() {
        return _mBackground;
    }

    public ContactListView setContainerBackground(int _mBackground) {
        this._mBackground = _mBackground;
        return this;
    }

    public ContactListView setContainerBackground(String _mContactBackground) {
        int color = 0;
        try {
            color = Color.parseColor(_mContactBackground);
        } catch (Exception e) {
            color = Color.parseColor("#FFFFFF");
        }
        this._mBackground = color;
        return this;
    }


    public int getContactBackground() {
        return _mContactBackground;
    }

    public void setContactBackground(int _mContactBackground) {
        this._mContactBackground = _mContactBackground;
    }

    public ContactListView setContactBackground(String _mContactBackground) {
        int color = 0;
        try {
            color = Color.parseColor(_mContactBackground);
        } catch (Exception e) {
            color = Color.parseColor("#FFFFFF");
        }
        this._mContactBackground = color;
        return this;
    }

    public int getContactDivider() {
        return _mContactDivider;
    }

    public void setContactDivider(int _mContactDivider) {
        this._mContactDivider = _mContactDivider;
    }

    public int getContactRowTxtColor() {
        return _mContactRowTxtColor;
    }

    public ContactListView setContactRowTxtColor(int _mContactRowTxtColor) {
        this._mContactRowTxtColor = _mContactRowTxtColor;
        return this;
    }


    public ContactListView setContactRowTxtColor(String _mContactBackground){
        int color = 0;
        try {
            color = Color.parseColor(_mContactBackground);
        } catch (Exception e) {
            color = Color.parseColor("#000000");
        }
        this._mContactRowTxtColor = color;
        return this;
    }



    public float getDividerHeight() {
        return _mDividerHeight;
    }

    public ContactListView setDividerHeight(float _mDividerHeight) {
        this._mDividerHeight = _mDividerHeight;
        return this;
    }

    public float getContactRowTxtSize() {
        return _mContactRowTxtSize;
    }

    public void setContactRowTxtSize(float _mContactRowTxtSize) {
        this._mContactRowTxtSize = _mContactRowTxtSize;
    }


    public boolean isAvatarVisible() {
        return isAvatarVisible;
    }

    public ContactListView setIsAvatarVisible(boolean isAvatarVisible) {
        this.isAvatarVisible = isAvatarVisible;
        return this;
    }

    public boolean isCacheEnable() {
        return isCacheEnable;
    }

    public ContactListView setIsCacheEnable(boolean isCacheEnable) {
        this.isCacheEnable = isCacheEnable;
        manageContactCache();
        return this;
    }

    public boolean isIndexable() {
        return isIndexable;
    }

    public ContactListView setIsIndexable(boolean isIndexable) {
        this.isIndexable = isIndexable;
        if(_mContainerView!=null) {
            configContactListview();
        }
        return this;
    }

    private void configContactListview() {
        if(_mContainerView!=null) {
            if(_mPinnedHeaderListView!=null){
                _mContainerView.removeView(_mPinnedHeaderListView);
                _mPinnedHeaderListView=null;
                _mSimpleIndexAdapter=null;
            }
            if(_mListview!=null){
                _mContainerView.removeView(_mListview);
                _mListview=null;
                _mSimpleAdapter=null;
            }

            if (isIndexable) {
                _mPinnedHeaderListView = new PinnedHeaderListView(mContext);
                _mPinnedHeaderListView.setDivider(null);
                _mPinnedHeaderListView.setDividerHeight((int) _mDividerHeight);
                _mPinnedHeaderListView.setScrollbarFadingEnabled(false);
                _mPinnedHeaderListView.setFastScrollEnabled(true);
                _mPinnedHeaderListView.setEnableHeaderTransparencyChanges(false);
                _mPinnedHeaderListView.setPinnedHeaderView(_mLayoutInflater.inflate(R.layout.pinned_header_listview_side_header, _mPinnedHeaderListView, false));
                LinearLayout.LayoutParams _mListLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
                _mPinnedHeaderListView.setLayoutParams(_mListLayoutParams);
                _mContainerView.addView(_mPinnedHeaderListView);
                _mSimpleIndexAdapter = new SimpleIndexContactAdapter(mContext, this, _mPeopleList, _mSelectedPeopleList, filterPeopleArrayList);
                _mPinnedHeaderListView.setAdapter(_mSimpleIndexAdapter);
                _mPinnedHeaderListView.setOnScrollListener(_mSimpleIndexAdapter);
                _mPinnedHeaderListView.setOnItemClickListener(this);
                _mPinnedHeaderListView.setSelector(mContext.getResources().getDrawable(R.drawable.listview_selector));
            } else {
                _mListview = new ListView(mContext);
                LinearLayout.LayoutParams _mListLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
                _mListview.setLayoutParams(_mListLayoutParams);
                _mContainerView.addView(_mListview);
                if (_mListview != null) {
                    _mListview.setDividerHeight((int) _mDividerHeight);
                }
                _mSimpleAdapter = new SimpleContactAdapter(mContext, this, _mPeopleList, _mSelectedPeopleList, filterPeopleArrayList);
                _mListview.setAdapter(_mSimpleAdapter);
                _mListview.setOnItemClickListener(this);
                _mListview.setSelector(R.drawable.listview_selector);
            }

        }
    }

    /**
     * Show loader while fetching contact from Async Task
     */
    public void showLoaderView() {
        if (isLoadershow()) {
            getProgressBarDialog().show();
        }
    }

    /**
     * Show loader while fetching contact from Async Task
     */
    public void hideLoaderView() {
        if (isLoadershow()) {
            getProgressBarDialog().dismiss();
        }
    }

    /**
     * Fetch all contact list in device
     *
     * @param peopleArrayList list of contact in device
     * @param isError         fetching error status true if error occur else false
     */
    @Override
    public void onPeopleData(ArrayList<People> peopleArrayList, boolean isError) {
        if (!isError) {
            if (peopleArrayList.size() > 0) {
                manageNoContactView(true);
                _mPeopleList.clear();
                filterPeopleArrayList.clear();
                _mPeopleList.addAll(peopleArrayList);
                filterPeopleArrayList.addAll(_mPeopleList);
                if (isCacheEnable)
                    cacheContactPeopleData(peopleArrayList);

                if (_mSimpleAdapter != null)
                    _mSimpleAdapter.notifyDataSetChanged();

                if (_mSimpleIndexAdapter != null)
                    _mSimpleIndexAdapter.notifyDataSetChanged();

            } else {
                manageNoContactView(false);
            }
        }
    }

    /**
     * Listener bind to get List in Activity/Fragment
     *
     * @param selectedListener
     */
    public void setOnSelectedPeopleListener(OnSelectedPeopleList selectedListener) {
        this.selectedListener = selectedListener;
    }

    /**
     * Listener bind to get Count of Selected People in Activity/Fragment
     *
     * @param _mPeopleCheckCountListener
     */
    public void setOnPeopleCheckCountListener(OnPeopleCheckCountListener _mPeopleCheckCountListener) {
        this._mPeopleCheckCountListener = _mPeopleCheckCountListener;
    }


    @Override
    public void onCheckBoxClicked() {
        if (mTitleName != null) {
            if (_mSelectedPeopleList.size() > 0) {
                mTitleName.setText(_mSelectedPeopleList.size() + " selected");
            } else {
                mTitleName.setText(mContext.getString(R.string.contact_list_title));
            }
        }

        if (_mPeopleCheckCountListener != null) {
            _mPeopleCheckCountListener.selectedPeopleCount(_mSelectedPeopleList.size());
        }

    }


    public void showErrorMessage(String message) {
        _mErrorText.setText(message);
        _mErrorText.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.bounce_fadeout);
        _mErrorText.setAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                _mErrorText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                _mErrorText.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    private void manageNoContactView(boolean isListshown) {
        if (isListshown) {
            _mContainerView.setVisibility(View.VISIBLE);
            _noContactView.setVisibility(View.GONE);
        } else {
            _mContainerView.setVisibility(View.GONE);
            _noContactView.setVisibility(View.VISIBLE);
        }
    }


    /**
     * Perfrom Filter
     *
     * @param charSequence
     */
    public void performSearchQuery(CharSequence charSequence) {

        if(!isIndexable()) {
            if (_mSimpleAdapter != null)
                _mSimpleAdapter.getFilter().filter(charSequence);
        }else{
            if(_mSimpleIndexAdapter!=null){
                _mSimpleIndexAdapter.getFilter().filter(charSequence);
                _mSimpleIndexAdapter.setHeaderViewVisible(TextUtils.isEmpty(charSequence));
            }
        }
    }

    /**
     * Perform Selected Done Button Event
     */
    public void performDone() {
        if (selectedListener != null) {
            if (_mSelectedPeopleList.size() > 0) {
                selectedListener.getSelectedPeopleList(_mSelectedPeopleList);
            } else {
                showErrorMessage(mContext.getString(R.string.please_select_people_to_proceed));
            }
        }
    }


    /**
     * Perform Select All / Unselect All based on Flag
     *
     * @param isSelectAll
     */
    public void performSelectAll(boolean isSelectAll) {
        _mSelectedPeopleList.clear();
        if (!selectFlag) {
            selectFlag = true;
            _mSelectedPeopleList.addAll(_mPeopleList);
        } else {
            selectFlag = false;
        }
        if (_mSimpleAdapter != null)
            _mSimpleAdapter.notifyDataSetChanged();

        if (_mSimpleIndexAdapter!= null)
            _mSimpleIndexAdapter.notifyDataSetChanged();
        onCheckBoxClicked();
    }

    /**
     * Get Select All/ UnSelect All Status
     *
     * @return selectFlag
     */
    public boolean getSelectAllStatus() {
        return selectFlag;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(!isMultiSelectEnable())
        if (selectedListener != null) {
            _mSelectedPeopleList.clear();
            _mSelectedPeopleList.add((People) parent.getAdapter().getItem(position));
             selectedListener.getSelectedPeopleList(_mSelectedPeopleList);
        }
    }

}
