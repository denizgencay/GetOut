package com.imc.getout.fragments.inviteFragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.AddressComponents;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.imc.getout.MainActivity;
import com.imc.getout.R;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

public class EventInviteFragment extends Fragment {

    private DatePickerDialog.OnDateSetListener dateSetListener;
    private TimePickerDialog.OnTimeSetListener timeSetListener;
    private String numberOfPersonText,timeText,dateText,expText,locationAddress,locationName,locationCity,locationState,titleText,name,userPhoto;
    private int numberOfPersonAsInt;
    private Timestamp expireDate;
    private TextView time,date,location,explanation,title,numberOfPerson;
    private Button sendEventInvite;
    private AddressComponents comp;
    private boolean eventAlreadyActive = false;
    private ArrayList<String> activeInviteUids;

    private List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,Place.Field.NAME,Place.Field.ADDRESS,Place.Field.ADDRESS_COMPONENTS);
    private String apikey = "AIzaSyB3zL_9d-2C-n6uyK3kQS-HQSB20qfWIuI";
    private int AUTOCOMPLETE_REQUEST_CODE = 1;
    private PlacesClient placesClient;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_invite_fragment,container,false);

        Places.initialize(getActivity(),apikey);
        placesClient = Places.createClient(getActivity());

        time = view.findViewById(R.id.eventTimeText);
        date = view.findViewById(R.id.eventDateText);
        location = view.findViewById(R.id.eventLocationText);
        explanation = view.findViewById(R.id.eventExplanationText);
        sendEventInvite = view.findViewById(R.id.sendEventInvite);
        numberOfPerson = view.findViewById(R.id.eventNumberOfPersonText);
        title = view.findViewById(R.id.eventTitleText);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,placeFields).build(getActivity());
                startActivityForResult(intent,AUTOCOMPLETE_REQUEST_CODE);
            }
        });

        DocumentReference documentReference = firebaseFirestore.collection("Users")
                .document(firebaseAuth.getCurrentUser().getUid());
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null){
                    Toast.makeText(getActivity(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                }

                if (documentSnapshot != null) {

                    name = documentSnapshot.getString("name");
                    userPhoto = documentSnapshot.getString("photoUri");
                    activeInviteUids = (ArrayList<String>) documentSnapshot.get("eventUids");

                    if (documentSnapshot.getBoolean("activeEvent")  && documentSnapshot.getBoolean("activeEvent") != null) {
                        eventAlreadyActive = true;
                    }
                }
            }
        });

        sendEventInvite.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                timeText = time.getText().toString();
                dateText = date.getText().toString();
                expText = explanation.getText().toString();
                locationAddress = location.getText().toString();
                numberOfPersonText = numberOfPerson.getText().toString();
                titleText = title.getText().toString();

                if (!timeText.isEmpty() && !expText.isEmpty() && !locationAddress.isEmpty()
                        && !dateText.isEmpty() && !numberOfPersonText.isEmpty() && !titleText.isEmpty()) {

                    numberOfPersonAsInt = Integer.parseInt(numberOfPersonText);

                    if (numberOfPersonAsInt > 1 && numberOfPersonAsInt < 101) {
                        if (!eventAlreadyActive) {

                            String[] hourAndMinute = timeText.split(":");
                            String[] dayMonthandYear = dateText.split("/");

                            int dayAsInt = Integer.parseInt(dayMonthandYear[0]);
                            int monthAsInt = Integer.parseInt(dayMonthandYear[1]);
                            int yearAsInt = Integer.parseInt(dayMonthandYear[2]);

                            int hourAsInt = Integer.parseInt(hourAndMinute[0]);
                            int minuteAsInt = Integer.parseInt(hourAndMinute[1]);
                            Date date;

                            LocalDateTime expire = LocalDateTime.of(yearAsInt,monthAsInt,dayAsInt,hourAsInt,minuteAsInt);
                            long expireInMillies = expire.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

                            date = new Date(expireInMillies);
                            expireDate = new Timestamp(date);

                            String email = firebaseAuth.getCurrentUser().getEmail();
                            String uid = firebaseAuth.getCurrentUser().getUid();
                            ArrayList<String> attendants = new ArrayList<>();
                            ArrayList<String> requests = new ArrayList<>();


                            HashMap<String,Object> postData = new HashMap<>();
                            postData.put("explanation",expText);
                            postData.put("userEmail",email);
                            postData.put("hour",timeText);
                            postData.put("date",dateText);
                            postData.put("locationField",locationName);
                            postData.put("numberOfPerson",numberOfPersonAsInt);
                            postData.put("userUid",uid);
                            postData.put("currentNumberOfPerson",1);
                            postData.put("locationAddress",locationAddress);
                            postData.put("locationCity",locationCity);
                            postData.put("locationState",locationState);
                            postData.put("expireDate",expireDate);
                            postData.put("title",titleText);
                            postData.put("name",name);
                            postData.put("userPhoto",userPhoto);

                            Task<DocumentReference> doc = firebaseFirestore.collection("events").add(postData);

                            doc.addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    if (activeInviteUids == null) {
                                        activeInviteUids = new ArrayList<>();
                                    }
                                    String id = documentReference.getId();
                                    activeInviteUids.add(id);
                                    HashMap<String,Object> update = new HashMap<>();
                                    update.put("activeEvent",true);
                                    update.put("eventUids",activeInviteUids);

                                    firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Intent intent = new Intent(getActivity(), MainActivity.class);
                                            getActivity().finish();
                                            startActivity(intent);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getActivity(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                        } else {
                            Toast.makeText(getActivity(),"Premium olmadan aynı anda sadece bir davet açabilirsiniz",Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getActivity(),"Lütfen kişi sayısını 2 ile 100 arasında giriniz", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getActivity(),"Lütfen bütün alanları doldurunuz",Toast.LENGTH_LONG).show();
                }
            }
        });

        //Saat seçimi ayarları
        timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                String h,m;
                if (i < 10) {
                    if (i1 < 10) {
                        h = "0"+i;
                        m = "0"+i1;
                    } else {
                        h = "0"+i;
                        m = String.valueOf(i1);
                    }
                } else {
                    if (i1 < 10) {
                        h = String.valueOf(i);
                        m = "0"+i1;
                    } else {
                        h = String.valueOf(i);
                        m = String.valueOf(i1);
                    }
                }
                time.setText(h+":"+m);
                timeText = time.getText().toString();
            }
        };
        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);


                TimePickerDialog dialog = new TimePickerDialog(getContext(), AlertDialog.THEME_HOLO_DARK,timeSetListener,
                        hour,minute,true);
                dialog.show();
            }
        });

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                GregorianCalendar selectedDate = new GregorianCalendar(i,i1,i2);
                int month = i1+1;
                date.setText(i2+"/"+month+"/"+i);
                dateText = date.getText().toString();
            }
        };
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(getActivity(),
                        AlertDialog.THEME_HOLO_LIGHT,dateSetListener,year,month,day);
                dialog.getDatePicker().setMinDate(new Date().getTime()-60000);
                dialog.show();
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == AutocompleteActivity.RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                location.setText(place.getAddress());
                locationName = place.getName();
                comp = place.getAddressComponents();
                List<AddressComponent> list = comp.asList();
                for (AddressComponent i : list) {
                    if (i.getTypes().get(0).equals("administrative_area_level_2")) {
                        locationState = i.getName().toLowerCase();
                    } else if (i.getTypes().get(0).equals("administrative_area_level_1")){
                        locationCity = i.getName().toLowerCase();
                    }
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
            } else if (resultCode == AutocompleteActivity.RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
}
