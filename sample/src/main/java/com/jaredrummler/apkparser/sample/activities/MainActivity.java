/*
 * Copyright (C) 2015. Jared Rummler <jared.rummler@gmail.com>
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
 *
 */

package com.jaredrummler.apkparser.sample.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.jaredrummler.apkparser.ApkParser;
import com.jaredrummler.apkparser.model.DexInfo;
import com.jaredrummler.apkparser.sample.R;
import com.jaredrummler.apkparser.sample.dialogs.XmlListDialog;
import com.jaredrummler.apkparser.sample.fragments.AppListFragment;
import com.jaredrummler.apkparser.sample.interfaces.ApkParserSample;
import com.jaredrummler.apkparser.sample.util.Helper;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends Activity implements ApkParserSample {

    private AppListFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            showFragment(true);
        }
        ActionBar actionBar = getActionBar();
        Log.d("MainActivity", "actionBar:" + actionBar);
        if (actionBar != null) {
            actionBar.setSubtitle(R.string.user_app);
//            TextView textView = new TextView(this);
//            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//            textView.setText("dsdsd");
//            textView.setTextColor(getResources().getColor(android.R.color.white));
//            actionBar.setCustomView(R.layout.search_view);
        }

        EditText editText = findViewById(R.id.et_search);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mFragment != null) {
                    mFragment.onSearchTextChange(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "onResume");
    }

    /**
     * @param userApp 是否只显示第三方应用
     */
    private void showFragment(boolean userApp) {
        mFragment = new AppListFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("user_app", userApp);
        mFragment.setArguments(bundle);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.content, mFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_filter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("MainActivity", "item.getItemId():" + item.getItemId());
        CharSequence title = item.getTitle();
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(title);
        }
        showFragment(item.getItemId() == R.id.user_app);
        return true;
    }

    @Override
    public void openXmlFile(PackageInfo app, String xml) {
        Intent intent = new Intent(this, XmlSourceViewerActivity.class);
        intent.putExtra("app", app);
        intent.putExtra("xml", xml);
        startActivity(intent);
    }

    @Override
    public void listXmlFiles(final PackageInfo app) {
        new ListXmlTask(this).execute(app);
    }

    static class ListXmlTask extends AsyncTask<PackageInfo, Void, String[]> {

        WeakReference<Activity> mWeakReference;
        private ProgressDialog mPd;
        private PackageInfo mInfo;

        public ListXmlTask(Activity context) {
            mWeakReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Context context = mWeakReference.get();
            if (context != null) {
                mPd = new ProgressDialog(context);
                mPd.setMessage("Please wait...");
                mPd.show();
            }
        }

        @Override
        protected String[] doInBackground(PackageInfo... packageInfos) {
            mInfo = packageInfos[0];
            return Helper.getXmlFiles(mInfo.applicationInfo.sourceDir);
        }

        @Override
        protected void onPostExecute(String[] items) {
            super.onPostExecute(items);
            mPd.dismiss();
            if (mWeakReference.get() != null) {
                XmlListDialog.show(mWeakReference.get(), mInfo, items);
            }
        }
    }

    @SuppressWarnings("AlibabaThreadPoolCreation")
    @Override
    public void showMethodCount(final PackageInfo app) {
        Executors.newCachedThreadPool().execute(() -> {
            ApkParser parser = ApkParser.create(app);
            try {
                List<DexInfo> dexInfos = parser.getDexInfos();
                int methodCount = 0;
                for (DexInfo dexInfo : dexInfos) {
                    methodCount += dexInfo.header.methodIdsSize;
                }
                String message = NumberFormat.getNumberInstance().format(methodCount);
                toast(message, Toast.LENGTH_SHORT);
            } catch (IOException e) {
                toast(e.getMessage(), Toast.LENGTH_LONG);
            } finally {
                parser.close();
            }
        });
    }

    private void toast(final String message, final int length) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(getApplicationContext(), message, length).show();
        } else {
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), message, length).show());
        }
    }
}
