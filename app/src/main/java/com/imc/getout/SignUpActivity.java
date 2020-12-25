package com.imc.getout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.imc.getout.models.SignUpModel;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SignUpActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "SignUpActivity";

    private TextView nameText,emailText,cityText,passwordText,passwordVerText,dateText,usernameText;
    private String gender,name,email,city,password,passwordVer,birthdayTextVal,username;
    private Timestamp birthdayText;
    private ImageView ppimage;
    private GregorianCalendar date;
    private Bitmap selectedImage;
    private Uri imageData;

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private DatePickerDialog.OnDateSetListener dateSetListener;
    private SignUpModel signUpModel;

    private List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,Place.Field.NAME,Place.Field.ADDRESS,Place.Field.ADDRESS_COMPONENTS);
    private String apikey = "AIzaSyB3zL_9d-2C-n6uyK3kQS-HQSB20qfWIuI";
    private final int AUTOCOMPLETE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Spinner spinner = findViewById(R.id.genderSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(SignUpActivity.this,R.array.gender_array,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        dateText = findViewById(R.id.birthday);
        ppimage = findViewById(R.id.ppimage);
        nameText = findViewById(R.id.name);
        emailText = findViewById(R.id.email);
        cityText = findViewById(R.id.city);
        passwordText = findViewById(R.id.password);
        passwordVerText = findViewById(R.id.passwordVer);
        usernameText = findViewById(R.id.usernameText);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        signUpModel = new SignUpModel();

        Places.initialize(this,apikey);

        getUsernames();

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                date = new GregorianCalendar(i,i1,i2);
                int month = i1+1;
                birthdayText = new Timestamp(date.getTime());
                dateText.setText(i2+"/"+month+"/"+i);
            }
        };
    }

    private void getUsernames() {
        CollectionReference collectionReference = firebaseFirestore.collection("Usernames");

        collectionReference.addSnapshotListener(SignUpActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null) {
                    System.out.println("136 " + queryDocumentSnapshots.size());
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        String usernameText = (String) snapshot.getString("username");

                        signUpModel.getUsernames().add(usernameText);
                    }
                }
            }
        });
    }

    public void onSignUpClicked(View view) {
        name = nameText.getText().toString();
        email = emailText.getText().toString();
        city = cityText.getText().toString();
        password = passwordText.getText().toString();
        passwordVer = passwordVerText.getText().toString();
        birthdayTextVal = dateText.getText().toString();
        username = usernameText.getText().toString();

        if (imageData != null && !name.isEmpty() && !email.isEmpty() && !city.isEmpty()
                && !password.isEmpty() && !passwordVer.isEmpty() && !gender.isEmpty() && !username.isEmpty()
                && !gender.equals("Seçiniz") && !birthdayTextVal.equals("Choose Your Birthday")) {
            if (!password.equals(passwordVer)) {
                Toast.makeText(this,"Passwords must be same",Toast.LENGTH_LONG).show();
            } else {
                if (username.length() < 6) {
                    Toast.makeText(this,"Kullanıcı adınız en az 6 karakterli olmalıdır",Toast.LENGTH_LONG).show();
                } else {
                    if (signUpModel.getUsernames().contains(username)) {
                        Toast.makeText(this, "Bu kullanıcı adı alınmıştır lütfen başka bir kullanıcı adı deneyiniz.", Toast.LENGTH_SHORT).show();
                    } else {
                        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                final String userUid = firebaseAuth.getCurrentUser().getUid();
                                final String imageName = userUid+"/ProfilePicture.jpg";
                                storageReference.child(imageName).putFile(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        StorageReference newReference = FirebaseStorage.getInstance().getReference(imageName);
                                        newReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                String downloadUri = uri.toString();

                                                ArrayList<String> friends = new ArrayList<>();
                                                ArrayList<String> friendRequests = new ArrayList<>();
                                                ArrayList<String> instantInviteUids = new ArrayList<>();
                                                ArrayList<String> plannedInviteUids = new ArrayList<>();
                                                ArrayList<String> eventUids = new ArrayList<>();
                                                ArrayList<String> lastSearched = new ArrayList<>();
                                                ArrayList<String> chats = new ArrayList<>();
                                                ArrayList<String> attendantInstantInviteUids = new ArrayList<>();
                                                ArrayList<String> attendantPlannedInviteUids = new ArrayList<>();
                                                ArrayList<String> attendantEventUids = new ArrayList<>();

                                                HashMap<String,Object> postData = new HashMap<>();
                                                postData.put("name",name);
                                                postData.put("email",email);
                                                postData.put("birthday",birthdayText);
                                                postData.put("photoUri",downloadUri);
                                                postData.put("gender",gender);
                                                postData.put("city",city);
                                                postData.put("username",username);
                                                postData.put("activeInstantInvite",false);
                                                postData.put("activePlannedInvite",false);
                                                postData.put("activeEvent",false);
                                                postData.put("friends",friends);
                                                postData.put("friendRequests",friendRequests);
                                                postData.put("isPremium",false);
                                                postData.put("instantInviteUids",instantInviteUids);
                                                postData.put("plannedInviteUids",plannedInviteUids);
                                                postData.put("eventUids",eventUids);
                                                postData.put("lastSearched",lastSearched);
                                                postData.put("chats",chats);
                                                postData.put("attendantInstantInviteUids",attendantInstantInviteUids);
                                                postData.put("attendantPlannedInviteUids",attendantPlannedInviteUids);
                                                postData.put("attendantEventUids",attendantEventUids);

                                                firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).set(postData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        CollectionReference collectionReference = firebaseFirestore.collection("Usernames");

                                                        HashMap<String,Object> data = new HashMap<>();
                                                        data.put("username",username);

                                                        collectionReference.document(userUid).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                CollectionReference collectionReference1 = firebaseFirestore.collection("Passwords");

                                                                HashMap<String,Object> passwordData = new HashMap<>();
                                                                passwordData.put("password",password);

                                                                collectionReference1.document(userUid).set(passwordData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                                                                            @Override
                                                                            public void onSuccess(InstanceIdResult instanceIdResult) {
                                                                                String token = instanceIdResult.getToken();

                                                                                HashMap<String,Object> tokenUpdate = new HashMap<>();
                                                                                tokenUpdate.put("pushToken",token);

                                                                                firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).set(tokenUpdate, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        Intent intent = new Intent(SignUpActivity.this, PhoneVerificationActivity.class);
                                                                                        finish();
                                                                                        startActivity(intent);
                                                                                    }
                                                                                });
                                                                            }
                                                                        });
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        System.out.println(e.getLocalizedMessage());
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(SignUpActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SignUpActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SignUpActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        } else {
            Toast.makeText(this,"Fill all the required areas",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        gender = adapterView.getItemAtPosition(i).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void onBirthdayClicked(View view){
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(SignUpActivity.this,
                AlertDialog.THEME_HOLO_LIGHT,dateSetListener,year,month,day);
        dialog.show();
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
                        ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(),imageData);
                        selectedImage = ImageDecoder.decodeBitmap(source);
                        ppimage.setImageBitmap(selectedImage);
                    } else {
                        selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageData);
                        ppimage.setImageBitmap(selectedImage);
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
                        cityText.setText(i.getName());
                        city = cityText.getText().toString();
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

    public void onCityClicked(View view) {
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,placeFields).setTypeFilter(TypeFilter.CITIES).build(this);
        startActivityForResult(intent,AUTOCOMPLETE_REQUEST_CODE);
    }
}
