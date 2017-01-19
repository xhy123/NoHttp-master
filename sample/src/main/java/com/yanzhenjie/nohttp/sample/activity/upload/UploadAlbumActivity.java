/*
 * Copyright 2015 Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yanzhenjie.nohttp.sample.activity.upload;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yanzhenjie.nohttp.sample.R;
import com.yanzhenjie.nohttp.sample.activity.BaseActivity;
import com.yanzhenjie.nohttp.sample.nohttp.HttpListener;
import com.yanzhenjie.nohttp.sample.util.Constants;
import com.yanzhenjie.nohttp.sample.util.Snackbar;
import com.yolanda.nohttp.FileBinary;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.OnUploadListener;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;
import com.yolanda.nohttp.tools.ImageLocalLoader;

import java.io.File;

/**
 * <p>从相册选择图片上传。</p>
 * Created on 2016/6/3.
 *
 * @author Yan Zhenjie;
 */
public class UploadAlbumActivity extends BaseActivity {

    /**
     * 相册选择回调。
     */
    private static final int RESULT_BACK_ALBUM = 0x01;

    /**
     * 展示选择的头像。
     */
    private ImageView mIvIcon;
    /**
     * 选择的图片路径。
     */
    private String mImagePath;
    /**
     * 显示状态。
     */
    private TextView mTvResult;
    /**
     * 显示进度。
     */
    private ProgressBar mProgressBar;

    @Override
    protected void onActivityCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_upload_album);
        findView(R.id.btn_album).setOnClickListener(onClickListener);
        findView(R.id.btn_start).setOnClickListener(onClickListener);

        mIvIcon = findView(R.id.iv_icon);
        mTvResult = findView(R.id.tv_result);
        mProgressBar = findView(R.id.pb_progress);
    }

    /**
     * 按钮点击。
     */
    private View.OnClickListener onClickListener = v -> {
        if (v.getId() == R.id.btn_album) {
            selectImageFormAlbum();
        } else if (v.getId() == R.id.btn_start) {
            upload();
        }
    };

    /**
     * 选择图片。
     */
    private void selectImageFormAlbum() {
        Intent picture = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(picture, RESULT_FIRST_USER);
    }

    /**
     * 上传图片。
     */
    private void upload() {
        if (TextUtils.isEmpty(mImagePath))
            Snackbar.show(this, R.string.upload_file_select_album_null);
        else {
            File file = new File(mImagePath);
            if (file.exists())
                executeUpload(file);
            else
                Snackbar.show(this, R.string.upload_file_select_album_null_again);
        }
    }

    /**
     * 执行上传任务。
     */
    private void executeUpload(File file) {
        Request<String> request = NoHttp.createStringRequest(Constants.URL_NOHTTP_UPLOAD, RequestMethod.POST);

        // 添加普通参数。
        request.add("user", "yolanda");

        // 上传文件需要实现NoHttp的Binary接口，NoHttp默认实现了FileBinary、InputStreamBinary、ByteArrayBitnary、BitmapBinary。
        FileBinary fileBinary0 = new FileBinary(file);
        /**
         * 监听上传过程，如果不需要监听就不用设置。
         * 第一个参数：what，what和handler的what一样，会在回调被调用的回调你开发者，作用是一个Listener可以监听多个文件的上传状态。
         * 第二个参数： 监听器。
         */
        fileBinary0.setUploadListener(0, mOnUploadListener);

        request.add("userHead", fileBinary0);// 添加1个文件

        request(0, request, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) {
                showMessageDialog(R.string.request_succeed, response.get());
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                showMessageDialog(R.string.request_failed, response.getException().getMessage());
            }
        }, false, true);
    }

    /**
     * 文件上传监听。
     */
    private OnUploadListener mOnUploadListener = new OnUploadListener() {

        @Override
        public void onStart(int what) {// 这个文件开始上传。
            mTvResult.setText(R.string.upload_start);
        }

        @Override
        public void onCancel(int what) {// 这个文件的上传被取消时。
            mTvResult.setText(R.string.upload_cancel);
        }

        @Override
        public void onProgress(int what, int progress) {// 这个文件的上传进度发生边耍
            mProgressBar.setProgress(progress);
        }

        @Override
        public void onFinish(int what) {// 文件上传完成
            mTvResult.setText(R.string.upload_succeed);
        }

        @Override
        public void onError(int what, Exception exception) {// 文件上传发生错误。
            mTvResult.setText(R.string.upload_error);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_BACK_ALBUM && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            getRealPathFromURI(uri);
            ImageLocalLoader.getInstance().loadImage(mIvIcon, mImagePath);
        }
    }

    public void getRealPathFromURI(Uri contentUri) {
        String[] filePathColumns = {MediaStore.MediaColumns.DATA};
        ContentResolver contentResolver = getContentResolver();
        Cursor c = contentResolver.query(contentUri, filePathColumns, null, null, null);
        if (c != null) {
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            mImagePath = c.getString(columnIndex);
            c.close();
        }
    }
}
