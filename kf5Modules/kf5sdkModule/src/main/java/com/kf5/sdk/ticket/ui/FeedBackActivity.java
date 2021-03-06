package com.kf5.sdk.ticket.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.util.ArrayMap;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chosen.album.Matisse;
import com.chosen.album.internal.utils.PathUtils;
import com.chosen.cameraview.ui.CameraActivity;
import com.kf5.sdk.R;
import com.kf5.sdk.system.album.ImageSelectorManager;
import com.kf5.sdk.system.base.BaseActivity;
import com.kf5.sdk.system.entity.ParamsKey;
import com.kf5.sdk.system.mvp.presenter.PresenterFactory;
import com.kf5.sdk.system.mvp.presenter.PresenterLoader;
import com.kf5.sdk.system.utils.CameraDisplayUtils;
import com.kf5.sdk.system.utils.SPUtils;
import com.kf5.sdk.system.utils.Utils;
import com.kf5.sdk.system.widget.ActionSheetDialog;
import com.kf5.sdk.ticket.mvp.presenter.TicketFeedBackPresenter;
import com.kf5.sdk.ticket.mvp.usecase.TicketUseCaseManager;
import com.kf5.sdk.ticket.mvp.view.ITicketFeedBackView;
import com.kf5.sdk.ticket.receiver.TicketReceiver;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

public class FeedBackActivity extends BaseActivity<TicketFeedBackPresenter, ITicketFeedBackView> implements ITicketFeedBackView, View.OnTouchListener, View.OnClickListener {

    private EditText mETContent;

    private ImageView mImgChoiceImg;

    private List<File> mFiles = new ArrayList<>();

    private LinearLayout mImgContainerLayout;

    private LinearLayout.LayoutParams mParams = null;

    private boolean isChangedStatus = false;

    private String[] CAMERA_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private String[] WRITE_EXTERNAL_STORAGE_PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private ImageView mBackImg;

    private TextView mTVSubmit;


    @Override
    protected int getLayoutID() {
        return R.layout.kf5_activity_feed_back;
    }


    @Override
    public Loader<TicketFeedBackPresenter> onCreateLoader(int id, Bundle args) {
        return new PresenterLoader<>(this, new PresenterFactory<TicketFeedBackPresenter>() {
            @Override
            public TicketFeedBackPresenter create() {
                return new TicketFeedBackPresenter(TicketUseCaseManager.provideTicketFeedBackCase());
            }
        });
    }

    @Override
    protected void initWidgets() {
        super.initWidgets();
        mImgContainerLayout = (LinearLayout) findViewById(R.id.kf5_feed_back_image_layout);
        mETContent = (EditText) findViewById(R.id.kf5_feed_back_content_et);
        mETContent.setOnTouchListener(this);
        mETContent.addTextChangedListener(new ETTextWatcher());
        mImgChoiceImg = (ImageView) findViewById(R.id.kf5_feed_back_choice_img);
        mImgChoiceImg.setOnClickListener(this);
        mParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mParams.bottomMargin = 1;
        mBackImg = (ImageView) findViewById(R.id.kf5_return_img);
        mBackImg.setOnClickListener(this);
        mTVSubmit = (TextView) findViewById(R.id.kf5_right_text_view);
        mTVSubmit.setOnClickListener(this);
        mTVSubmit.setEnabled(false);
    }

    @Override
    public void createTicketSuccess() {
        runOnUiThread(new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setAction(TicketReceiver.TICKET_FILTER);
                sendBroadcast(intent);
                showToast(getString(R.string.kf5_create_ticket_successfully));
                finish();
            }
        });
    }

    @Override
    public Map<String, String> getDataMap() {
//        String title = getIntent().getStringExtra("title");
//        String content = getIntent().getStringExtra("content");
//        String category = getIntent().getStringExtra("category");
//        String phone = getIntent().getStringExtra("phone");
//        String project = getIntent().getStringExtra("project");

        ArrayMap<String, String> map = new ArrayMap<>();
        map.put("title", "Android_关于拍照功能_18940980522");
        map.put("content", mETContent.getText().toString());
        //map.put("priority", "high"); //可选值："low", "medium", "high", "urgent"

        //用户自定义字段
        try {
            JSONArray jsonArray = new JSONArray();

            JSONObject field_1012285 = new JSONObject();
            field_1012285.put(ParamsKey.NAME, "field_1012285");//问题分类
            field_1012285.put(ParamsKey.VALUE, "关于拍照功能");
            jsonArray.put(field_1012285);

            JSONObject field_1012286 = new JSONObject();
            field_1012286.put(ParamsKey.NAME, "field_1012286");//手机号
            field_1012286.put(ParamsKey.VALUE, "18940980522");
            jsonArray.put(field_1012286);

            JSONObject field_1012239 = new JSONObject();
            field_1012239.put(ParamsKey.NAME, "field_1012239");//项目名称
            field_1012239.put(ParamsKey.VALUE, "黑人");
            jsonArray.put(field_1012239);

            map.put(ParamsKey.CUSTOM_FIELDS, jsonArray.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    @Override
    public void showError(int resultCode, final String msg) {
        super.showError(resultCode, msg);
        runOnUiThread(new TimerTask() {
            @Override
            public void run() {
                showToast(msg);
            }
        });
    }

    @Override
    public List<File> getUploadFileList() {
        return mFiles;
    }

    @Override
    public void loadUploadData(final Map<String, String> map) {
        runOnUiThread(new TimerTask() {
            @Override
            public void run() {
                presenter.createTicket(map);
            }
        });
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int id = view.getId();
        if (id == R.id.kf5_feed_back_content_et) {
            if (!mETContent.hasFocus()) {
                mETContent.setFocusableInTouchMode(true);
            }
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.kf5_right_text_view) {
            if (!Utils.isNetworkUable(mActivity)) {
                showToast(getString(R.string.kf5_no_internet));
                return;
            }
            if (mFiles.size() > 0) {
                showDialog = true;
                presenter.uploadAttachment();
            } else {
                showDialog = true;
                presenter.createTicket(null);
            }
        } else if (id == R.id.kf5_return_img) {
            finish();
        } else if (id == R.id.kf5_feed_back_choice_img) {
            Utils.hideSoftInput(mActivity, mETContent);
            dealSelectImage();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CAMERA_STATE:
                if (hasPermission(CAMERA_PERMISSIONS))
                    CameraDisplayUtils.cameraDisplayOnlyPictureFeature(this, CAMERA);
                break;
            case WRITE_EXTERNAL_STORAGE:
                if (hasPermission(WRITE_EXTERNAL_STORAGE_PERMISSIONS))
//                    getPictureFromGallery(6 - mFiles.size());
                    ImageSelectorManager.toTicketImageGallery(this, GALLERY);
                break;
            default:
                if (resultCode == RESULT_OK) {
                    switch (requestCode) {
                        case CAMERA:
                            try {
                                String type = data.getStringExtra(CameraActivity.TYPE);
                                String path = data.getStringExtra(CameraActivity.PATH);
                                File file = new File(path);
                                mFiles.add(file);
                                if (mImgContainerLayout.getVisibility() == View.GONE) {
                                    mImgContainerLayout.setVisibility(View.VISIBLE);
                                }
                                mImgContainerLayout.addView(getView(file), mParams);
                                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                Uri uri = Uri.fromFile(file);
                                intent.setData(uri);
                                sendBroadcast(intent);
//                                if (picFile != null && picFile.exists()) {
//                                    mFiles.add(picFile);
//                                    if (mImgContainerLayout.getVisibility() == View.GONE) {
//                                        mImgContainerLayout.setVisibility(View.VISIBLE);
//                                    }
//                                    mImgContainerLayout.addView(getView(picFile), mParams);
//                                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                                    Uri uri = Uri.fromFile(picFile);
//                                    intent.setData(uri);
//                                    sendBroadcast(intent);
//                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case GALLERY:
                            try {
                                if (data != null) {
                                    List<Uri> selectedResult = Matisse.obtainResult(data);
                                    for (Uri uri : selectedResult) {
                                        String path = PathUtils.getPath(this, uri);
                                        if (!TextUtils.isEmpty(path)) {
                                            File file = new File(path);
                                            String name = file.getName();
                                            String suffix = name.substring(name.lastIndexOf('.') + 1, name.length());
                                            if (Utils.isImage(suffix)) {
                                                if (file.exists()) {
                                                    mFiles.add(file);
                                                    if (mImgContainerLayout.getVisibility() == View.GONE)
                                                        mImgContainerLayout.setVisibility(View.VISIBLE);
                                                    mImgContainerLayout.addView(getView(file), mParams);
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }
                break;
        }


    }


    /**
     * 选择图片或者拍照图片
     */
    private void dealSelectImage() {


        if (mFiles.size() < 6) {
            new ActionSheetDialog(mActivity)
                    .builder()
                    .setCancelable(true)
                    .setCanceledOnTouchOutside(true)
                    .addSheetItem(getString(R.string.kf5_from_camera), ActionSheetDialog.SheetItemColor.Blue,
                            new ActionSheetDialog.OnSheetItemClickListener() {
                                @Override
                                public void onClick(int which) {
                                    if (hasPermission(CAMERA_PERMISSIONS)) {
                                        CameraDisplayUtils.cameraDisplayOnlyPictureFeature(FeedBackActivity.this, CAMERA);
                                    } else
                                        applyPermissions(CAMERA_STATE, METHOD_REQUEST_PERMISSION, CAMERA_PERMISSIONS);
                                }
                            })
                    .addSheetItem(getString(R.string.kf5_from_gallery), ActionSheetDialog.SheetItemColor.Blue,
                            new ActionSheetDialog.OnSheetItemClickListener() {
                                @Override
                                public void onClick(int which) {
                                    if (hasPermission(WRITE_EXTERNAL_STORAGE_PERMISSIONS)) {
                                        ImageSelectorManager.toTicketImageGallery(mActivity, GALLERY);
                                    } else {
                                        applyPermissions(WRITE_EXTERNAL_STORAGE, METHOD_REQUEST_PERMISSION, WRITE_EXTERNAL_STORAGE_PERMISSIONS);
                                    }
                                }
                            }).show();
        } else {
            showToast(getString(R.string.kf5_file_limit_hint));
        }
    }


    private View getView(File file) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.kf5_upload_attach_item, null);
        TextView name = (TextView) view.findViewById(R.id.kf5_value);
        ImageView type = (ImageView) view.findViewById(R.id.kf5_type_icon);
        TextView scan = (TextView) view.findViewById(R.id.kf5_scan);
        name.setText(file.getName());
        scan.setOnClickListener(new OnItemListener(file, view));
        return view;
    }

    private class OnItemListener implements View.OnClickListener {

        private File file;

        private View view;

        public OnItemListener(File file, View view) {
            this.file = file;
            this.view = view;
        }

        @Override
        public void onClick(View v) {
            mImgContainerLayout.removeView(view);
            mFiles.remove(file);
            if (mFiles.size() == 0) {
                mImgContainerLayout.setVisibility(View.GONE);
            }
        }
    }


    private class ETTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable string) {
            if (string.toString().trim().length() > 0) {
                if (!isChangedStatus) {
                    isChangedStatus = true;
                    mTVSubmit.setEnabled(true);
                }
            } else {
                isChangedStatus = false;
                mTVSubmit.setEnabled(false);
            }
        }
    }


}
