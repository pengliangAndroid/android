package com.weex.app;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import java.io.File;

public class UpdateProvider extends FileProvider {
    public UpdateProvider() {
    }

    public static Uri getUriForFile(Context context, File file) {
        return getUriForFile(context, context.getPackageName() + ".update.provider", file);
    }
}
