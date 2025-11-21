package com.example.lotterypatentpending.helpers;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.lotterypatentpending.models.FirebaseManager;

import java.util.List;

public class TagDropdownHelper {

    /**
     * Initializes an AutoCompleteTextView with tags from Firestore.
     *
     * @param context   usually Activity.this or fragment.requireContext()
     * @param dropdown  the AutoCompleteTextView to populate
     * @param fm        your FirebaseManager instance
     */
    public static void setupTagDropdown(@NonNull Context context, @NonNull AutoCompleteTextView dropdown, @NonNull FirebaseManager fm) {

        fm.getAllEventTags(new FirebaseManager.FirebaseCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> tags) {
                if (tags == null || tags.isEmpty()) {
                    Toast.makeText(context,
                            "No tags found. Create some",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        context,
                        android.R.layout.simple_list_item_1,
                        tags
                );
                dropdown.setAdapter(adapter);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context,
                        "Failed to load tags: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
