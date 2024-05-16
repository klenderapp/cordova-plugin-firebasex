package org.apache.cordova.firebase;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SoundManager {

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static Uri addSoundToSystem(Context context) throws IOException {
        String soundTitle = "Klender - Sound";

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.TITLE, soundTitle);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, soundTitle);
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_NOTIFICATIONS);
        values.put(MediaStore.Audio.AudioColumns.IS_RINGTONE, false);
        values.put(MediaStore.Audio.AudioColumns.IS_NOTIFICATION, true);
        values.put(MediaStore.Audio.AudioColumns.IS_ALARM, true);
        values.put(MediaStore.Audio.AudioColumns.IS_MUSIC, false);

        Uri contentUri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

        // Checking for existence
        String[] projection = new String[] { MediaStore.MediaColumns._ID };
        String selection = MediaStore.MediaColumns.TITLE + "=?";
        String[] selectionArgs = new String[] { soundTitle };

        try (Cursor cursor = context.getContentResolver().query(contentUri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                // File exists, return the existing Uri
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
                return ContentUris.withAppendedId(contentUri, id);
            }
        }

        Uri newUri = context.getContentResolver().insert(contentUri, values);

        if (newUri != null) {
            // Get the resource identifier dynamically
            int resourceId = context.getResources().getIdentifier("notification_sound", "raw", context.getPackageName());
            if (resourceId == 0) {
                throw new RuntimeException("Resource not found");
            }
            InputStream inputStream = context.getResources().openRawResource(resourceId);
            OutputStream outputStream = context.getContentResolver().openOutputStream(newUri);
            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) > 0) {
                if (outputStream != null) {
                    outputStream.write(buffer, 0, read);
                }
            }
            inputStream.close();
            if (outputStream != null) {
                outputStream.close();
            }
        } else {
            throw new IOException("Uri is null");
        }

        return newUri;
    }
}

