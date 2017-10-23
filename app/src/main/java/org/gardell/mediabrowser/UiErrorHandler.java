package org.gardell.mediabrowser;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.agera.Function;
import com.google.android.agera.Result;

import static android.util.Log.getStackTraceString;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;
import static com.google.android.agera.Result.failure;

final class UiErrorHandler<T> implements Function<Throwable, Result<T>> {

    @NonNull
    private final Activity activity;
    private final boolean lengthLong;

    UiErrorHandler(@NonNull Activity activity, boolean lengthLong) {
        this.activity = activity;
        this.lengthLong = lengthLong;
    }

    @NonNull
    @Override
    public Result<T> apply(@NonNull Throwable input) {
        activity.runOnUiThread(() -> Toast.makeText(
                activity,
                input.getLocalizedMessage() + "\n" + getStackTraceString(input),
                lengthLong ? LENGTH_LONG : LENGTH_SHORT)
                .show());
        return failure(input);
    }
}
