package org.gardell.mediabrowser;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

final class StartPostponedEnterTransitionRequestListener
        implements RequestListener<Drawable> {

    @NonNull
    private final Activity activity;

    StartPostponedEnterTransitionRequestListener(@NonNull Activity activity) {
        this.activity = activity;
    }

    @Override
    public boolean onLoadFailed(
            @Nullable GlideException e,
            Object model,
            Target<Drawable> target,
            boolean isFirstResource) {
        activity.startPostponedEnterTransition();
        return false;
    }

    @Override
    public boolean onResourceReady(
            Drawable resource,
            Object model,
            Target<Drawable> target,
            DataSource dataSource,
            boolean isFirstResource) {
        activity.startPostponedEnterTransition();
        return false;
    }
}
