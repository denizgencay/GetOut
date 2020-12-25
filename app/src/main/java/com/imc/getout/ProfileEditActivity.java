package com.imc.getout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.AddressComponents;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.imc.getout.fragments.mainFragments.ProfileFragment;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ProfileEditActivity extends AppCompatActivity {

    private Button save;
    private TextView city;
    private EditText aboutMe,name;
    private ImageView profilePhoto;
    private String cityText,aboutMeText,nameText;
    private Bitmap selectedImage;
    private Uri imageData;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    private List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,Place.Field.NAME,Place.Field.ADDRESS,Place.Field.ADDRESS_COMPONENTS);
    private String apikey = "AIzaSyB3zL_9d-2C-n6uyK3kQS-HQSB20qfWIuI";
    private int AUTOCOMPLETE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        save = findViewById(R.id.profile_edit_save);
        city = findViewById(R.id.profile_edit_city);
        aboutMe = findViewById(R.id.profile_edit_about_me);
        name = findViewById(R.id.profile_edit_name);
        profilePhoto = findViewById(R.id.profile_edit_profile_photo);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        Places.initialize(this,apikey);

        getDataFromFirestore();

    }

    private void getDataFromFirestore() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        DocumentReference documentReference = firebaseFirestore.collection("Users").document(uid);

        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    String cityText = (String) documentSnapshot.getString("city");
                    String aboutMeText = (String) documentSnapshot.getString("aboutMe");
                    String photoUrl = (String) documentSnapshot.getString("photoUri");
                    String nameText = (String) documentSnapshot.getString("name");

                    city.setText(cityText);
                    aboutMe.setText(aboutMeText);
                    name.setText(nameText);
                    Picasso.get().load(photoUrl).noFade().into(profilePhoto);

                }
            }
        });
    }

    public void cityClicked(View view) {
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,placeFields).setTypeFilter(TypeFilter.CITIES).build(this);
        startActivityForResult(intent,AUTOCOMPLETE_REQUEST_CODE);
    }

    public void onSaveButtonClicked(View view) {
        cityText = city.getText().toString();
        aboutMeText = aboutMe.getText().toString();
        nameText = name.getText().toString();

        final String uid = firebaseAuth.getCurrentUser().getUid();

        final String imageName = uid+"/ProfilePicture.jpg";

        if (imageData != null) {
            storageReference.child(imageName).putFile(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    StorageReference newReference = firebaseStorage.getReference(imageName);
                    newReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            HashMap<String,Object> update = new HashMap<>();
                            update.put("city",cityText);
                            update.put("aboutMe",aboutMeText);
                            update.put("photoUri",uri.toString());
                            update.put("name",nameText);
                            final String finalUri = uri.toString();

                            DocumentReference documentReference = firebaseFirestore.collection("Users").document(uid);

                            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    final ArrayList<String> instantInviteUids = (ArrayList<String>) documentSnapshot.get("instantInviteUids");
                                    final ArrayList<String> plannedInviteUids = (ArrayList<String>) documentSnapshot.get("plannedInviteUids");
                                    final ArrayList<String> eventUids = (ArrayList<String>) documentSnapshot.get("eventUids");

                                    if (instantInviteUids != null) {
                                        for (String instantInviteUid : instantInviteUids) {
                                            HashMap<String,Object> inviteUpdate = new HashMap<>();
                                            inviteUpdate.put("name",nameText);
                                            inviteUpdate.put("userPhoto",finalUri);

                                            DocumentReference documentReference1 = firebaseFirestore.collection("instantInvites").document(instantInviteUid);

                                            documentReference1.set(inviteUpdate,SetOptions.merge());
                                        }
                                    }

                                    if (plannedInviteUids != null) {
                                        for (String plannedInviteUid : plannedInviteUids) {
                                            HashMap<String,Object> inviteUpdate = new HashMap<>();
                                            inviteUpdate.put("name",nameText);
                                            inviteUpdate.put("userPhoto",finalUri);

                                            DocumentReference documentReference1 = firebaseFirestore.collection("plannedInvites").document(plannedInviteUid);

                                            documentReference1.set(inviteUpdate,SetOptions.merge());
                                        }
                                    }

                                    if (eventUids != null) {
                                        for (String eventUid : eventUids) {
                                            HashMap<String,Object> inviteUpdate = new HashMap<>();
                                            inviteUpdate.put("name",nameText);
                                            inviteUpdate.put("userPhoto",finalUri);

                                            DocumentReference documentReference1 = firebaseFirestore.collection("events").document(eventUid);

                                            documentReference1.set(inviteUpdate,SetOptions.merge());
                                        }
                                    }
                                }
                            });

                            documentReference.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(ProfileEditActivity.this, "Kaydedildi", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ProfileEditActivity.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfileEditActivity.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileEditActivity.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                }
            });
        } else {
            HashMap<String,Object> update = new HashMap<>();
            update.put("city",cityText);
            update.put("aboutMe",aboutMeText);
            update.put("name",nameText);

            DocumentReference documentReference = firebaseFirestore.collection("Users").document(uid);

            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    final ArrayList<String> instantInviteUids = (ArrayList<String>) documentSnapshot.get("instantInviteUids");
                    final ArrayList<String> plannedInviteUids = (ArrayList<String>) documentSnapshot.get("plannedInviteUids");
                    final ArrayList<String> eventUids = (ArrayList<String>) documentSnapshot.get("eventUids");

                    if (instantInviteUids != null) {
                        for (String instantInviteUid : instantInviteUids) {
                            HashMap<String,Object> inviteUpdate = new HashMap<>();
                            inviteUpdate.put("name",nameText);

                            DocumentReference documentReference1 = firebaseFirestore.collection("instantInvites").document(instantInviteUid);

                            documentReference1.set(inviteUpdate,SetOptions.merge());
                        }
                    }

                    if (plannedInviteUids != null) {
                        for (String plannedInviteUid : plannedInviteUids) {
                            HashMap<String,Object> inviteUpdate = new HashMap<>();
                            inviteUpdate.put("name",nameText);

                            DocumentReference documentReference1 = firebaseFirestore.collection("plannedInvites").document(plannedInviteUid);

                            documentReference1.set(inviteUpdate,SetOptions.merge());
                        }
                    }

                    if (eventUids != null) {
                        for (String eventUid : eventUids) {
                            HashMap<String,Object> inviteUpdate = new HashMap<>();
                            inviteUpdate.put("name",nameText);

                            DocumentReference documentReference1 = firebaseFirestore.collection("events").document(eventUid);

                            documentReference1.set(inviteUpdate,SetOptions.merge());
                        }
                    }
                }
            });

            documentReference.set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(ProfileEditActivity.this, "Kaydedildi", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileEditActivity.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void onImageSelect(View view){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},1);
        } else {
            CropImage.activity()
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setFixAspectRatio(true)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageData = result.getUri();
                try {
                    if (Build.VERSION.SDK_INT >= 28) {
                        ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), imageData);
                        selectedImage = ImageDecoder.decodeBitmap(source);
                        profilePhoto.setImageBitmap(selectedImage);
                    } else {
                        selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageData);
                        profilePhoto.setImageBitmap(selectedImage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        } else if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == AutocompleteActivity.RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                AddressComponents comp = place.getAddressComponents();
                List<AddressComponent> list = comp.asList();
                for (AddressComponent i : list) {
                    if (i.getTypes().get(0).equals("administrative_area_level_1")) {
                        city.setText(i.getName());
                        cityText = city.getText().toString();
                    }
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
            } else if (resultCode == AutocompleteActivity.RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
