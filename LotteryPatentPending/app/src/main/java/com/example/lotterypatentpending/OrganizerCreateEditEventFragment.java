package com.example.lotterypatentpending;

import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterypatentpending.helpers.DateTimeFormatHelper;
import com.example.lotterypatentpending.helpers.DateTimePickerHelper;
import com.example.lotterypatentpending.helpers.TagDropdownHelper;
import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.viewModels.UserEventRepository;
import com.example.lotterypatentpending.viewModels.EventViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;

import android.net.Uri;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Fragment that allows users to create a new Event or edit an existing one.
 * <p>
 * Collects input from the user, validates fields, creates or updates an Event object,
 * stores it in Firestore, and navigates to OrganizerEventViewFragment to display the event.
 * Handles image selection and inline storage of event posters.
 * </p>
 * @author
 * @contributor Erik
 */
public class OrganizerCreateEditEventFragment extends Fragment {

    /** EditText for the event's title. */
    private EditText titleEt;
    /** EditText for the event's description. */
    private EditText descriptionEt;
    /** EditText for the event's location. */
    private EditText locationEt;
    /** EditText for the maximum number of entrants. */
    private EditText capacityEt;
    /** EditText for the waiting list capacity. */
    private EditText waitingListCapEt;
    /** EditText for inputting a new tag to add to the dropdown. */
    private EditText newTagInput;
    /** Button to add a new tag. */
    private MaterialButton addTagButton;
    /** AutoCompleteTextView for selecting or typing an event tag. */
    private AutoCompleteTextView tagDropdown;
    /** TextView displaying the page title ("Create New Event" or "Edit Event"). */
    private TextView pageTitle;
    /** TextView for selecting and displaying the event's date and time. */
    private TextView eventDateEt;
    /** TextView for selecting and displaying the registration start date and time. */
    private TextView regStartDateEt;
    /** TextView for selecting and displaying the registration end date and time. */
    private TextView regEndDateEt;
    /** Button to cancel the operation and go back. */
    private Button cancelBtn;
    /** Button to create or save the event. */
    private Button createEditBtn;
    /** Firebase Manager instance for database operations. */
    private FirebaseManager fm;
    /** ViewModel to share the Event object between fragments. */
    private EventViewModel viewModel;

    /** ImageView to display a preview of the selected poster. */
    private ImageView posterPreview;
    /** Button to trigger the image selection from the gallery. */
    private Button btnSelectPoster;
    /** URI of the currently selected poster image. */
    private Uri selectedPosterUri = null;

    /**
     * Inflates the fragment's layout.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views.
     * @param container The parent ViewGroup.
     * @param savedInstanceState Bundle containing saved instance state.
     * @return The root View of the fragment's layout.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_fragment_create_edit_event, container, false);
    }

    /**
     * Called after the view has been created.
     * Initializes UI elements, sets click listeners for buttons, and populates fields if editing an existing event.
     *
     * @param v The root view of the fragment.
     * @param savedInstanceState Bundle containing saved instance state.
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        //Firebase Manager Instance
        fm = FirebaseManager.getInstance();

        //Set the views we'll be using
        pageTitle = v.findViewById(R.id.pageTitleText);
        titleEt = v.findViewById(R.id.titleEt);
        tagDropdown = v.findViewById(R.id.tagDropdown);
        newTagInput = v.findViewById(R.id.newTagInput);
        addTagButton = v.findViewById(R.id.addTagButton);
        descriptionEt = v.findViewById(R.id.descriptionEt);
        locationEt = v.findViewById(R.id.locationEt);
        eventDateEt = v.findViewById(R.id.eventDateEt);
        regStartDateEt = v.findViewById(R.id.registrationStartDate);
        regEndDateEt = v.findViewById(R.id.registrationEndDate);
        capacityEt = v.findViewById(R.id.maxEntrantsInput);
        waitingListCapEt = v.findViewById(R.id.waitingListCapInput);
        cancelBtn = v.findViewById(R.id.cancelButton);
        createEditBtn = v.findViewById(R.id.createEventButton);

        viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
        Event passed_event = viewModel.getEvent().getValue();

        posterPreview = v.findViewById(R.id.posterImage);
        btnSelectPoster = v.findViewById(R.id.btnSelectPoster);

        btnSelectPoster.setOnClickListener(view -> {
            // open gallery for an image
            pickImageLauncher.launch("image/*");
        });


        if(passed_event != null){
            (requireActivity()).setTitle("Edit Event");
            pageTitle.setText("Edit Event");
            titleEt.setText(passed_event.getTitle());
            TagDropdownHelper.setupTagDropdown(requireContext(), tagDropdown, fm);
            tagDropdown.setText(passed_event.getTag());
            descriptionEt.setText(passed_event.getDescription());
            locationEt.setText(passed_event.getLocation());
            eventDateEt.setText(DateTimeFormatHelper.formatTimestamp(passed_event.getDate()));
            regStartDateEt.setText(DateTimeFormatHelper.formatTimestamp(passed_event.getRegStartDate()));
            regEndDateEt.setText(DateTimeFormatHelper.formatTimestamp(passed_event.getRegEndDate()));
            capacityEt.setText(String.valueOf(passed_event.getCapacity()));

            int wlCap = passed_event.getWaitingListCapacity();

            if(wlCap == -1){
                waitingListCapEt.setText(null);
            }else{
                waitingListCapEt.setText(String.valueOf(passed_event.getWaitingListCapacity()));
            }

            byte[] posterBytes = passed_event.getPosterBytes();
            if (posterBytes != null && posterBytes.length > 0) {
                Bitmap bmp = BitmapFactory.decodeByteArray(posterBytes, 0, posterBytes.length);
                posterPreview.setImageBitmap(bmp);
            }

            createEditBtn.setText("Save Changes");
            createEditBtn.setOnClickListener(view -> {
                if (createEditEvent("edit", passed_event)) {
                    NavHostFragment.findNavController(OrganizerCreateEditEventFragment.this)
                            .navigate(R.id.action_createEditEvent_to_Event_View, null,
                                    new NavOptions.Builder()
                                            .setPopUpTo(R.id.CreateEditEventFragment, true)
                                            .build());
                }
            }); // removes OrganizerCreateEventFragment from stack so when back is clicked from event view doesn't go back there goes back to page beforehand
        }else{
            (requireActivity()).setTitle("Create Event");
            pageTitle.setText("Create New Event");
            createEditBtn.setOnClickListener(view -> {
                if (createEditEvent("create", passed_event)) {
                    NavHostFragment.findNavController(OrganizerCreateEditEventFragment.this)
                            .navigate(R.id.action_createEditEvent_to_Event_View, null,
                                    new NavOptions.Builder()
                                            .setPopUpTo(R.id.CreateEditEventFragment, true)
                                            .build());
                }
            }); // removes OrganizerCreateEventFragment from stack so when back is clicked from event view doesn't go back there goes back to page beforehand
        }


        //Attach adapter to tagDropdown
        TagDropdownHelper.setupTagDropdown(requireContext(), tagDropdown, fm);

        //Add onClickListener for adding tag
        addTagButton.setOnClickListener(view -> {
            String rawTag = newTagInput.getText().toString().trim();
            if (rawTag.isEmpty()) {
                Toast.makeText(requireContext(),
                        "Please enter a tag name.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            fm.addEventTag(rawTag, new FirebaseManager.FirebaseCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    // Clear input
                    newTagInput.setText("");

                    // Refresh dropdown options
                    TagDropdownHelper.setupTagDropdown(requireContext(), tagDropdown, fm);

                    // Optionally pre-fill dropdown with the new tag
                    // normalized same way as addEventTag
                    String normalized = rawTag.substring(0, 1).toUpperCase()
                            + rawTag.substring(1).toLowerCase();
                    tagDropdown.setText(normalized, false);

                    Toast.makeText(requireContext(),
                            "Tag added.",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(requireContext(),
                            "Failed to add tag: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Attach date Dialog and time Dialog for each TextView date
        DateTimePickerHelper.attachDateTimePicker(eventDateEt, requireContext());
        DateTimePickerHelper.attachDateTimePicker(regStartDateEt, requireContext());
        DateTimePickerHelper.attachDateTimePicker(regEndDateEt, requireContext());

        cancelBtn.setOnClickListener(view ->
                NavHostFragment.findNavController(OrganizerCreateEditEventFragment.this)
                        .popBackStack());

    }

    /**
     * Collects input from EditText/TextViews fields, creates or updates an Event object,
     * saves it to Firestore, and updates the {@link EventViewModel}.
     *
     * @param action The action type, either "create" for a new event or "edit" for an existing one.
     * @param passed_event The existing {@link Event} object if editing, otherwise null.
     * @return true if the event was successfully created/edited and validation passed, false otherwise.
     */
    public boolean createEditEvent(String action, Event passed_event) {
        String title = titleEt.getText().toString().trim();
        String tag = tagDropdown.getText().toString().trim();
        String description = descriptionEt.getText().toString().trim();
        String location = locationEt.getText().toString().trim();
        String eventDateString = eventDateEt.getText().toString().trim();
        String regStartDateString = regStartDateEt.getText().toString().trim();
        String regEndDateString = regEndDateEt.getText().toString().trim();
        String capacityString = capacityEt.getText().toString().trim();
        String waitingListCapString= waitingListCapEt.getText().toString().trim();

        // Basic required fields
        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Title and description are required.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if(location.isEmpty()){location = null;}

        // Dates required (strings)
        if (eventDateString.isEmpty() || regStartDateString.isEmpty() || regEndDateString.isEmpty()) {

            Toast.makeText(requireContext(),
                    "Event date, registration start, and registration end are required.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        // Capacity required and must be a number
        if (capacityString.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Event capacity is required.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        int capacity;
        try {
            capacity = Integer.parseInt(capacityString);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(),
                    "Capacity must be a number.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        int waitingListCap = -1;
        if (!waitingListCapString.equalsIgnoreCase("N/A") &&
                !waitingListCapString.isEmpty()) {
            try {
                waitingListCap = Integer.parseInt(waitingListCapString);
                if(waitingListCap < capacity){
                    Toast.makeText(requireContext(),
                            "Waiting list cap must be greater than or equal to event capacity.",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(),
                        "Waiting list cap must be a number or 'N/A'.",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        // Parse timestamps
        Timestamp eventDate = DateTimeFormatHelper.parseTimestamp(eventDateString);
        Timestamp regStartDate = DateTimeFormatHelper.parseTimestamp(regStartDateString);
        Timestamp regEndDate = DateTimeFormatHelper.parseTimestamp(regEndDateString);

        // Parsing failed -> invalid date format
        if (eventDate == null || regStartDate == null || regEndDate == null) {
            Toast.makeText(requireContext(),
                    "Please select valid dates and times.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        // Registration window rules
        if (!validateRegistrationWindow(eventDate, regStartDate, regEndDate)) {
            return false;
        }

        // Get the current user
        User current_user = UserEventRepository.getInstance().getUser().getValue();
        if (current_user == null) {
            Toast.makeText(requireContext(),
                    "No current user logged in.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }



        if(action.equals("create") && passed_event == null){
            // Create the new event
            Event newEvent = new Event(title, description, capacity, current_user);
            newEvent.setTag(tag);
            newEvent.setLocation(location);
            newEvent.setDate(eventDate);
            newEvent.setRegStartDate(regStartDate);
            newEvent.setRegEndDate(regEndDate);
            newEvent.setWaitingListCapacity(waitingListCap);

            byte[] posterBytes = loadPosterBytesFromUri();
            if (posterBytes != null) {
                newEvent.setPosterBytes(posterBytes);
                Log.d("Poster", "Created event posterBytes length=" + posterBytes.length);
            }

            // Save to Firestore
            fm.addEventToDB(newEvent);
            viewModel.setEvent(newEvent);


        }else if(action.equals("edit") && passed_event != null){
            passed_event.setTitle(title);
            passed_event.setDescription(description);
            passed_event.setTag(tag);
            passed_event.setLocation(location);
            passed_event.setDate(eventDate);
            passed_event.setRegStartDate(regStartDate);
            passed_event.setRegEndDate(regEndDate);
            passed_event.setCapacity(capacity);
            passed_event.setWaitingListCapacity(waitingListCap);

            byte[] posterBytes = loadPosterBytesFromUri();
            if (posterBytes != null) {
                passed_event.setPosterBytes(posterBytes);
                Log.d("Poster", "Updated event posterBytes length=" + posterBytes.length);
            }

            fm.addOrUpdateEvent(passed_event.getId(), passed_event);
            viewModel.setEvent(passed_event);

        }


        // Update EventViewModel for sharing with OrganizerEventViewFragment


        return true;
    }


    /**
     * Validates the chronological order of the registration window relative to the event date.
     *
     * @param eventDate timestamp of the events date
     * @param regStart timestamp of the registration start date
     * @param regEnd timestamp of the registration end date
     * @return true if the timestamps are validated and make sense (regStart < regEnd <= eventDate), false otherwise.
     */
    private boolean validateRegistrationWindow(Timestamp eventDate,
                                               Timestamp regStart,
                                               Timestamp regEnd) {
        if (eventDate == null || regStart == null || regEnd == null) {
            Toast.makeText(requireContext(),
                    "Please set event date, registration start, and registration end.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        // regEnd must be AFTER regStart (strict)
        if (regEnd.compareTo(regStart) <= 0) {
            Toast.makeText(requireContext(),
                    "Registration end must be after registration start.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        // regEnd must NOT be after eventDate
        if (regEnd.compareTo(eventDate) > 0) {
            Toast.makeText(requireContext(),
                    "Registration must end before the event starts.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Loads bitmap from selectedPosterUri, then calls FirebaseManager.uploadEventPosterInline.
     * @param eventId The ID of the event to associate the poster with (currently unused in this implementation).
     */
    private void uploadPosterInline(String eventId) {
        if (selectedPosterUri == null) return;

        Bitmap bmp;
        try {
            bmp = loadBitmapFromUri(selectedPosterUri);
        } catch (IOException e) {
            e.printStackTrace();
            if (isAdded()) {
                Toast.makeText(requireContext(),
                        "Failed to read image: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
            return;
        }
    }

    /**
     * Helper for decoding a Bitmap from a Uri, handling pre/post API 28.
     * @param uri The URI of the image content.
     * @return The decoded Bitmap.
     * @throws IOException If the image cannot be decoded.
     */
    private Bitmap loadBitmapFromUri(@NonNull Uri uri) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.Source src =
                    ImageDecoder.createSource(requireContext().getContentResolver(), uri);
            return ImageDecoder.decodeBitmap(src);
        } else {
            return MediaStore.Images.Media.getBitmap(
                    requireContext().getContentResolver(),
                    uri
            );
        }
    }

    /**
     * Reads the image data from {@code selectedPosterUri}, scales it down to {@code 600px} max side,
     * compresses it as JPEG (70% quality), and returns the resulting byte array.
     *
     * @return The scaled and compressed poster image as a byte array, or null if reading fails.
     */
    private byte[] loadPosterBytesFromUri() {
        if (selectedPosterUri == null) return null;

        try (InputStream in = requireContext()
                .getContentResolver()
                .openInputStream(selectedPosterUri)) {

            if (in == null) return null;

            Bitmap original = BitmapFactory.decodeStream(in);
            if (original == null) return null;

            // Scale down so it fits comfortably in Firestore doc size limits
            int maxSide = 600;
            int w = original.getWidth();
            int h = original.getHeight();
            int longerSide = Math.max(w, h);

            float scale = longerSide > maxSide
                    ? (float) maxSide / longerSide
                    : 1f;

            int newW = Math.round(w * scale);
            int newH = Math.round(h * scale);

            Bitmap scaled = Bitmap.createScaledBitmap(original, newW, newH, true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // JPEG, 70% quality to reduce size further
            scaled.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            return baos.toByteArray();

        } catch (IOException e) {
            Log.e("CreateEditEvent", "Failed to read poster image", e);
            return null;
        }
    }

    /** Activity result launcher to handle selecting an image from the device's content provider. */
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            selectedPosterUri = uri;
                            if (posterPreview != null) {
                                posterPreview.setImageURI(uri);
                                posterPreview.setVisibility(View.VISIBLE);
                            }
                        }
                    });
}