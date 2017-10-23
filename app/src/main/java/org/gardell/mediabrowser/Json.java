package org.gardell.mediabrowser;

import android.support.annotation.NonNull;

import com.google.android.agera.Result;

import org.gardell.mediabrowser.model.Program;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.google.android.agera.Result.failure;
import static com.google.android.agera.Result.success;

final class Json {

    @NonNull
    static Result<List<Program>> programsFromJson(@NonNull String input) {
        try {
            final JSONArray programsJson = new JSONObject(input).getJSONArray("programs");
            final List<Program> programs =
                    new ArrayList<>(programsJson.length());
            for (int i = 0; i < programsJson.length(); ++i) {
                final JSONObject jsonProgram =
                        programsJson.getJSONObject(i);
                programs.add(programFromJson(jsonProgram));
            }
            return success(programs);
        } catch (JSONException | MalformedURLException e) {
            return failure(e);
        }
    }

    @NonNull
    static Result<Program> programFromJson(@NonNull String input) {
        try {
            return success(programFromJson(new JSONObject(input).getJSONObject("program")));
        } catch (JSONException | MalformedURLException e) {
            return failure(e);
        }
    }

    @NonNull
    private static Program programFromJson(@NonNull JSONObject jsonProgram)
            throws JSONException, MalformedURLException {
            return new Program(
                    jsonProgram.getInt("id"),
                    new URL(jsonProgram.getString("programimage")),
                    jsonProgram.getString("name"),
                    jsonProgram.getString("description"));
    }
}
