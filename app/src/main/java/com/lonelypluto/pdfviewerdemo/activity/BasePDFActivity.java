package com.lonelypluto.pdfviewerdemo.activity;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.app.Instrumentation;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.artifex.mupdfdemo.Hit;
import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.MuPDFPageAdapter;
import com.artifex.mupdfdemo.MuPDFReaderView;
import com.artifex.mupdfdemo.MuPDFReaderViewListener;
import com.artifex.mupdfdemo.MuPDFView;
import com.artifex.mupdfdemo.OutlineActivityData;
import com.artifex.mupdfdemo.ReaderView;
import com.lonelypluto.pdfviewerdemo.Contains;
import com.lonelypluto.pdfviewerdemo.MainActivity;
import com.lonelypluto.pdfviewerdemo.R;
import com.lonelypluto.pdfviewerdemo.RealPathUtil;

import java.util.concurrent.Executor;

/**
 * @Description: 基础功能仅显示pdf
 * @author: ZhangYW
 * @time: 2019/3/11 15:56
 */
public class BasePDFActivity extends AppCompatActivity {
    private static final String TAG = BasePDFActivity.class.getSimpleName();

    private String filePath = Environment.getExternalStorageDirectory() + "/pdf_t1.pdf"; // 文件路径

    private MuPDFCore muPDFCore;// 加载mupdf.so文件
    private MuPDFReaderView muPDFReaderView;// 显示pdf的view

//    private ActivityResultLauncher<Intent> mLauncherDocument =registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<Instrumentation.ActivityResult>() {
//        @Override
//        public void onActivityResult(Instrumentation.ActivityResult result) {
//            if (result.getResultCode()==RESULT_OK){
//                String path= RealPathUtil.getInstance().getRealPath(BasePDFActivity.this,result.getData().getData());
//                muPDFCore = openFile(filePath);
//                initView();
//            }
//        }
//    });




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_pdf);
        openDocument();
//        initView();
    }

    /**
     * 初始化
     */
    private void initView() {

        muPDFReaderView = (MuPDFReaderView)findViewById(R.id.open_pdf_mupdfreaderview);
        // 通过MuPDFCore打开pdf文件
//        muPDFCore = openFile(filePath);
        // 判断如果core为空，提示不能打开文件
        if (muPDFCore == null) {
            AlertDialog alert = new AlertDialog.Builder(this).create();
            alert.setTitle(com.lonelypluto.pdflibrary.R.string.cannot_open_document);
            alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(com.lonelypluto.pdflibrary.R.string.dismiss),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            alert.setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });
            alert.show();
            return;
        }
        // 显示
        muPDFReaderView.setAdapter(new MuPDFPageAdapter(this, muPDFCore));
    }

    /**
     * 打开文件
     * @param path 文件路径
     * @return
     */
    private MuPDFCore openFile(String path) {
        Log.e(TAG, "Trying to open " + path);
        try {
            muPDFCore = new MuPDFCore(this, path);
        } catch (Exception e) {
            Log.e(TAG, "openFile catch:" + e.toString());
            return null;
        } catch (OutOfMemoryError e) {
            //  out of memory is not an Exception, so we catch it separately.
            Log.e(TAG, "openFile catch: OutOfMemoryError " + e.toString());
            return null;
        }
        return muPDFCore;
    }

    @Override
    protected void onStart() {
        if (muPDFCore != null) {
            muPDFCore.startAlerts();
        }
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (muPDFCore != null) {
            muPDFCore.stopAlerts();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (muPDFReaderView != null) {
            muPDFReaderView.applyToChildren(new ReaderView.ViewMapper() {
                public void applyToView(View view) {
                    ((MuPDFView) view).releaseBitmaps();
                }
            });
        }
        if (muPDFCore != null)
            muPDFCore.onDestroy();
        muPDFCore = null;
        super.onDestroy();
    }

    void openDocument() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(Contains.MIME_TYPE_PDF);
        startActivityForResult(intent,123);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==123){
            if (resultCode==RESULT_OK){
                String path= RealPathUtil.getInstance().getRealPath(BasePDFActivity.this,data.getData());
                muPDFCore = openFile(path);
                initView();
            }
        }
    }
}
