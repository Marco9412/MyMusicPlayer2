package com.panni.mymusicplayer2.youtubedl;

import android.content.Context;

public abstract class LinkGetter {

    public abstract boolean setup(Context context);

    public abstract boolean available(Context context);

    public abstract String getLink(String url);

    public abstract boolean updateModule();

    /*public LinkGetter getLinkGetter(Context context) {
        LinkGetter res = new NativePythonLinkGetter();
        if (res.available(context)) {
            return res;
        }

        throw new RuntimeException("No LinkGetter objects available!");
    }*/
}
