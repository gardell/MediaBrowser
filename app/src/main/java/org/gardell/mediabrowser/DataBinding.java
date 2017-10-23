package org.gardell.mediabrowser;

import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;

import java.net.URL;

public final class DataBinding {
    @BindingAdapter({
            "receiver",
            "receiver_argument"
    })
    public static <T> void setViewReceivers(
            @NonNull final View view,
            @NonNull final ClickListener<T> receiver,
            @NonNull final T argument) {
        view.setOnClickListener(v -> receiver.onClick(argument, v));
    }

    @BindingAdapter({"image_url", "image_url_listener"})
    public static void setImageViewUrl(
            @NonNull ImageView imageView,
            @NonNull URL url,
            @NonNull RequestListener<Drawable> listener) {
        Glide
                .with(imageView)
                .load(url)
                .listener(listener)
                .into(imageView);
    }

    @BindingAdapter({"image_url"})
    public static void setImageViewUrl(
            @NonNull ImageView imageView,
            @NonNull URL url) {
        Glide
                .with(imageView)
                .load(url)
                .into(imageView);
    }
}
