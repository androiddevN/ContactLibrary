package com.trivedi.contact.UI;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.trivedi.contact.UI.Adapter.SimpleContactAdapter;
import com.trivedi.contact.UI.Model.People;
import com.trivedi.contact.UI.Task.ContactThreadSync;
import com.trivedi.contact.UI.listener.DataCallback;
import com.trivedi.contact.UI.listener.OnCheckedListener;
import com.trivedi.contact.UI.listener.OnSelectedPeopleList;
import com.trivedi.contact.UI.utils.InitiateSearch;

import java.util.ArrayList;

/**
 * Created by Neeraj on 26/5/16.
 */
public class ContactListView extends FrameLayout implements DataCallback,OnCheckedListener {

    Context mContext;
    /**
     * List View to inflate Contact list
     */
    private ListView _mListview;
    /**
     * Header view like Action bar view to show selected contact count and done button.
     */
    private View _mHeaderView;

    private LinearLayout _mContainerView;

    private boolean _mHeadershow = false;

    private boolean _mLoadershow = false;

    private int _mContactBackground;

    private int _mContactDivider;

    private int _mContactRowTxtColor;

    private float _mDividerHeight = 5.0f;      // set Default value 5.0

    private float _mContactRowTxtSize = 14.0f;

    private boolean isAvatarVisible = false;

    private boolean isNameOnly= true;

    private boolean isSearchEnable= true;

    private boolean isMultiSelectEnable= false;

    private int _mHeaderBgColor;

    private int _mBackground;

    private float _mAvatarSize=38.0f;

    private TextView _mErrorText;

    private ArrayList<People> _mPeopleList = new ArrayList<>();

    private ArrayList<People> _mSelectedPeopleList = new ArrayList<>();

    private ArrayList<People> filterPeopleArrayList = new ArrayList<>();

    private SimpleContactAdapter _mSimpleAdapter;

    private LayoutInflater _mLayoutInflater;

    View _headerView;

    OnSelectedPeopleList selectedListener;

    boolean selectFlag=false;           // flag to use select all/Unselect all

    ProgressDialog _mProgressDailog;

    /**
     *  Header View Component
     */
    EditText _mEditSearchView;
    ImageButton _mSearchButton ;
    ImageButton _mDoneButton ;
    ImageButton _mSelectButton;
    ImageView _mClearSearch;
    CardView _SearchCardView ;
    RelativeLayout _mSearchButtonContainer;
    ImageView _mSearchBack;
    TextView mTitleName;




    public ContactListView(Context context) {
        this(context, null);
    }

    public ContactListView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.ContactStyle);
    }

    public ContactListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        final TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ContactListView,defStyleAttr, 0);
        _mContactBackground = attributes.getColor(R.styleable.ContactListView_contact_row_background, getResources().getColor(R.color.WHITE));
        _mContactDivider = attributes.getColor(R.styleable.ContactListView_contact_row_divider_color, getResources().getColor(R.color.BLACK));
        _mContactRowTxtColor = attributes.getColor(R.styleable.ContactListView_contact_row_text_color, getResources().getColor(R.color.BLACK));
        _mContactRowTxtSize = attributes.getDimension(R.styleable.ContactListView_contact_row_text_size, 14.0f);
        _mAvatarSize = attributes.getDimension(R.styleable.ContactListView_contact_row_text_size, 38.0f);
        _mHeadershow = attributes.getBoolean(R.styleable.ContactListView_show_header, false);
        _mLoadershow = attributes.getBoolean(R.styleable.ContactListView_show_loader, false);
        isAvatarVisible = attributes.getBoolean(R.styleable.ContactListView_show_avatar, false);
        isNameOnly = attributes.getBoolean(R.styleable.ContactListView_show_name_only, false);
        isSearchEnable = attributes.getBoolean(R.styleable.ContactListView_search_enable, true);
        isMultiSelectEnable = attributes.getBoolean(R.styleable.ContactListView_multiselect_enable, false);
        _mDividerHeight = attributes.getDimension(R.styleable.ContactListView_row_divider_height, 5.0f);
        _mBackground = attributes.getColor(R.styleable.ContactListView_contact_background, getResources().getColor(R.color.WHITE));
        _mHeaderBgColor = attributes.getColor(R.styleable.ContactListView_header_bg_color, getResources().getColor(R.color.primary));
        _mLayoutInflater = LayoutInflater.from(context);

         init();            // Initilize View Container
    }

    /**
     * Initlize view and attribute
     */
    private void init() {

        LayoutParams _mContainerParams = new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        _mContainerView = new LinearLayout(mContext);
        _mContainerView.setLayoutParams(_mContainerParams);
        _mContainerView.setWeightSum(1);
        _mContainerView.setOrientation(LinearLayout.VERTICAL);
        _mContainerView.setBackgroundColor(_mBackground);
        if (isHeadershow() || isMultiSelectEnable() || isSearchEnable()) {
            headerViewInitilize();
        }
        _mListview = new ListView(mContext);
        LinearLayout.LayoutParams _mListLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
        _mListview.setLayoutParams(_mListLayoutParams);
        _mContainerView.addView(_mListview);
        this.addView(_mContainerView);
        configContactListview();

        LayoutParams _mErrorContainer = new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        _mErrorText=new TextView(mContext);
        _mErrorText.setPadding(30, 30, 30, 30);
        _mErrorText.setTextColor(getResources().getColor(R.color.WHITE));
        _mErrorText.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        _mErrorText.setLayoutParams(_mErrorContainer);
        _mErrorText.setText(R.string.please_select_people_to_proceed);
        _mErrorText.setTypeface(null, Typeface.BOLD_ITALIC);
        _mErrorText.setGravity(Gravity.CENTER);
        _mErrorText.setVisibility(View.GONE);
        this.addView(_mErrorText);
        new ContactThreadSync(mContext, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        _mClearSearch= (ImageView) _headerView.findViewById(R.id.clearSearch);
        _SearchCardView= (CardView) _headerView.findViewById(R.id.card_search);
        _mSearchButtonContainer = (RelativeLayout) _headerView.findViewById(R.id.view_search);
        _mSearchBack = (ImageView) _headerView.findViewById(R.id.image_search_back);
        _mSearchButtonContainer.setBackgroundColor(_mHeaderBgColor);

        if(!isSearchEnable()){
            _SearchCardView.setVisibility(View.GONE);
            _mSearchButton.setVisibility(View.GONE);
        }
        if(!isMultiSelectEnable()){
            _mDoneButton.setVisibility(View.GONE);
            _mSelectButton.setVisibility(View.GONE);
        }else{
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
                    if(!selectFlag) {
                        selectFlag=true;
                        _mSelectedPeopleList.addAll(_mPeopleList);
                    }else{
                        selectFlag=false;
                    }
                    if(_mSimpleAdapter!=null)
                    _mSimpleAdapter.notifyDataSetChanged();
                    onCheckBoxClicked();
                }
            });
        }

        _mContainerView.addView(_headerView);

        _mSearchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                initiateSearch.handleToolBar(mContext, _SearchCardView, _mSearchButtonContainer, _mEditSearchView);
            }
        });
        _mSearchBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                initiateSearch.handleToolBar(mContext, _SearchCardView, _mSearchButtonContainer, _mEditSearchView);
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



    private void showProgressBarDialog(){
        _mProgressDailog=new ProgressDialog(mContext);
        _mProgressDailog.setMessage(mContext.getString(R.string.please_wait_message));
        _mProgressDailog.setTitle(mContext.getString(R.string.fetching_contact_title));
        _mProgressDailog.setIndeterminate(false);
        _mProgressDailog.setMax(100);
        _mProgressDailog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        _mProgressDailog.setCancelable(false);
        _mProgressDailog.setCanceledOnTouchOutside(false);
    }


    public ProgressDialog getProgressBarDialog(){
        if(_mProgressDailog==null){
            showProgressBarDialog();
        }
        return _mProgressDailog;
    }


    public float getAvatarSize() {
        return _mAvatarSize;
    }

    public void setAvatarSize(float _mAvatarSize) {
        this._mAvatarSize = _mAvatarSize;
    }


    public boolean isMultiSelectEnable() {
        return isMultiSelectEnable;
    }

    public void setIsMultiSelectEnable(boolean isMultiSelectEnable) {
        this.isMultiSelectEnable = isMultiSelectEnable;
    }

    public boolean isSearchEnable() {
        return isSearchEnable;
    }

    public void setIsSearchEnable(boolean isSearchEnable) {
        this.isSearchEnable = isSearchEnable;
    }

    public boolean isNameOnly() {
        return isNameOnly;
    }

    public void setIsNameOnly(boolean isNameOnly) {
        this.isNameOnly = isNameOnly;
    }

    public boolean isHeadershow() {
        return _mHeadershow;
    }

    public void setHeaderEnable(boolean _mHeadershow) {
        this._mHeadershow = _mHeadershow;
    }

    public boolean isLoadershow() {
        return _mLoadershow;
    }

    public void setLoaderEnable(boolean _mLoadershow) {
        this._mLoadershow = _mLoadershow;
    }

    public int getContactBackground() {
        return _mContactBackground;
    }

    public void setContactBackground(int _mContactBackground) {
        this._mContactBackground = _mContactBackground;
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

    public void setContactRowTxtColor(int _mContactRowTxtColor) {
        this._mContactRowTxtColor = _mContactRowTxtColor;
    }

    public float getDividerHeight() {
        return _mDividerHeight;
    }

    public void setDividerHeight(float _mDividerHeight) {
        this._mDividerHeight = _mDividerHeight;
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

    public void setIsAvatarVisible(boolean isAvatarVisible) {
        this.isAvatarVisible = isAvatarVisible;
    }

    private void configContactListview() {
        if (_mListview != null) {
            _mListview.setDividerHeight((int) _mDividerHeight);
        }
        _mSimpleAdapter = new SimpleContactAdapter(mContext, this, _mPeopleList,_mSelectedPeopleList,filterPeopleArrayList);
        _mListview.setAdapter(_mSimpleAdapter);
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
            _mPeopleList.clear();
            filterPeopleArrayList.clear();
            _mPeopleList.addAll(peopleArrayList);
            filterPeopleArrayList.addAll(peopleArrayList);
            if (_mSimpleAdapter != null)
                _mSimpleAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Listener bind to get List in Activity/Fragment
     * @param selectedListener
     */
    public void setOnSelectedPeopleListener(OnSelectedPeopleList selectedListener){
        this.selectedListener=selectedListener;
    }

    @Override
    public void onCheckBoxClicked() {
        if(_mSelectedPeopleList.size()>0){
            mTitleName.setText(_mSelectedPeopleList.size()+" selected");
        }else{
            mTitleName.setText(mContext.getString(R.string.contact_list_title));
        }
    }


    private void showErrorMessage(String message){
        _mErrorText.setText(message);
        _mErrorText.setVisibility(View.VISIBLE);
         Animation animation= AnimationUtils.loadAnimation(mContext,R.anim.bounce_fadeout);
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
}
