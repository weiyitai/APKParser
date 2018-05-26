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

package com.jaredrummler.apkparser.sample.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.pm.PackageInfo;
import android.os.Bundle;

import com.jaredrummler.apkparser.sample.interfaces.ApkParserSample;
import com.jaredrummler.apkparser.sample.util.AppNames;

public class XmlListDialog extends DialogFragment {

    public static void show(Activity activity, PackageInfo app, String[] items) {
        XmlListDialog dialog = new XmlListDialog();
        Bundle args = new Bundle();
        args.putParcelable("app", app);
        args.putStringArray("items", items);
        dialog.setArguments(args);
        dialog.show(activity.getFragmentManager(), "XmlListDialog");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final PackageInfo app = getArguments().getParcelable("app");
        final String[] items = getArguments().getStringArray("items");
        return new AlertDialog.Builder(getActivity())
                .setTitle(AppNames.getLabel(getActivity().getPackageManager(), app))
                .setItems(items, (dialog, which) -> {
                    if (getActivity() instanceof ApkParserSample) {
                        ApkParserSample callback = (ApkParserSample) getActivity();
                        callback.openXmlFile(app, items[which]);
                    }
                })
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                .create();
    }
}
