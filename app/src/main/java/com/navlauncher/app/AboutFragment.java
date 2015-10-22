package com.navlauncher.app;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.navlauncher.app.settings.Settings;

public class AboutFragment extends Fragment {
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.about_fragment, container);

        final TextView aboutTextView = (TextView) rootView.findViewById(R.id.txtAbout);
        final TextView urlTextView = (TextView) rootView.findViewById(R.id.txtUrl);

        String version = "1.0";

        try {
            final PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (final NameNotFoundException e) {
            if (Settings.LOGGING_ENABLED) {
                e.printStackTrace();
            }
        }

        aboutTextView.setText(getString(R.string.aboutText).replace("[VERSION]", version));

        final SpannableString url = SpannableString.valueOf(getString(R.string.url));
        Linkify.addLinks(url, Linkify.ALL);

        urlTextView.setText(url);

        urlTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.url)));
                startActivity(intent);
            }
        });

        return rootView;
    }
}
