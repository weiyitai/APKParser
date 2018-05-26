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

package com.jaredrummler.apkparser.sample.fragments;

import android.app.ListFragment;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;

import com.jaredrummler.apkparser.sample.adapters.AppListAdapter;
import com.jaredrummler.apkparser.sample.dialogs.AppDialog;
import com.jaredrummler.apkparser.sample.util.AppNames;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppListFragment extends ListFragment implements AdapterView.OnItemClickListener {

    private final ArrayList<PackageInfo> installedPackages = new ArrayList<>();
    private Parcelable listState;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            ArrayList<PackageInfo> packages = savedInstanceState.getParcelableArrayList("packages");
            if (packages != null && !packages.isEmpty()) {
                installedPackages.addAll(packages);
            }
            listState = savedInstanceState.getParcelable("state");
        } else {
            boolean userApp = getArguments().getBoolean("user_app");
            new AppLoader(userApp).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("packages", installedPackages);
        outState.putParcelable("state", getListView().onSaveInstanceState());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AppDialog.show(getActivity(), installedPackages.get(position));
    }

    private final class AppLoader extends AsyncTask<Void, Void, List<PackageInfo>> {

        private final boolean mUserApp;

        public AppLoader(boolean userApp) {
            mUserApp = userApp;
        }

        @Override
        protected List<PackageInfo> doInBackground(Void... params) {
            if (installedPackages.isEmpty()) {
                final PackageManager pm = getActivity().getPackageManager();
                List<PackageInfo> installedPackages = pm.getInstalledPackages(0);
                for (PackageInfo packageInfo : installedPackages) {
                    if (mUserApp) {
                        if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                            AppListFragment.this.installedPackages.add(packageInfo);
                        }
                    } else {
                        if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                            AppListFragment.this.installedPackages.add(packageInfo);
                        }
                    }
                }
                Collections.sort(AppListFragment.this.installedPackages, (lhs, rhs) -> AppNames.getLabel(pm, lhs).compareToIgnoreCase(AppNames.getLabel(pm, rhs)));
            }
            return installedPackages;
        }

        @Override
        protected void onPostExecute(List<PackageInfo> apps) {
            setListAdapter(new AppListAdapter(getActivity(), apps));
            getListView().setFastScrollEnabled(true);
            if (listState != null) {
                getListView().onRestoreInstanceState(listState);
                listState = null;
            }
        }
    }

}
