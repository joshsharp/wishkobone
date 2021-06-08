package au.com.joshsharp.wishkobone;

import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public abstract class JsonCallback implements Callback {

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        Object json;
        try {
            json = new JSONTokener(response.body().string()).nextValue();
            response.body().close();
        } catch (JSONException | NullPointerException e) {
            throw new IOException(e);
        }

        if (json instanceof JSONArray) {
            if (response.isSuccessful()) {
                this.onResponse(call, response, ((JSONArray) json));
            } else {
                this.onFailureResponse(call, response, ((JSONArray) json));
            }
        } else if (json instanceof JSONObject) {
            if (response.isSuccessful()) {
                this.onResponse(call, response, ((JSONObject) json));
            } else {
                this.onFailureResponse(call, response, ((JSONObject) json));
            }
        } else {
            this.onFailure(call, new IOException("Couldn't parse json"));
        }
    }

    public void onResponse(@NotNull Call call, @NotNull Response r, @NotNull JSONObject response) {
        // pass
        Log.d("okhttp", "empty callback for JSONObject called");
    }

    public void onResponse(@NotNull Call call, @NotNull Response r, @NotNull JSONArray response) {
        // pass
        Log.d("okhttp", "empty callback for JSONArray called");
    }

    public void onFailureResponse(@NotNull Call call, @NotNull Response r, @NotNull JSONObject response) {
        Log.d("okhttp", "empty failure callback for JSONObject called");
    }

    public void onFailureResponse(@NotNull Call call, @NotNull Response r, @NotNull JSONArray response) {
        Log.d("okhttp", "empty failure callback for JSONArray called");
    }
}
