package in.afi.codekosh.components;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;

public class FirebaseUtils {
    private final DatabaseReference Users = FirebaseDatabase.getInstance().getReference("Users");
    private HashMap<String, Object> hashMap = new HashMap<>();

    public FirebaseUtils() {
    }

    public void uploadFileToStorage(String reference, String child, Uri fileUri, StorageUploadCallback callback) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference(reference);

        // Create a child reference in the storage path
        StorageReference fileRef = storageRef.child(child);

        // Upload the file to Firebase Storage
        UploadTask uploadTask = fileRef.putFile(fileUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // File upload success
            // Get the download URL of the uploaded file
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String url = uri.toString();
                callback.onSuccess(url);
            }).addOnFailureListener(callback::onFailed);
        }).addOnFailureListener(callback::onFailed);
    }

    public void deleteFileFromStorageByUrl(String url) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storage.getReferenceFromUrl(url);
    }

    public void deleteFileFromStorageByUrl(String... urls) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        for (String url : urls) {
            storage.getReferenceFromUrl(url);
        }
    }

    public void increaseUserKeyData(String key, String uid) {
        getDataFromDatabase("Users", uid, database -> checkNull(database, () -> {
            int newLike = Integer.parseInt(String.valueOf(database.get(key))) + 1;
            hashMap = new HashMap<>();
            hashMap.put(key, String.valueOf(newLike));
            pushToDatabase(hashMap, "Users", uid, unused -> {

            }, e -> {

            });
        }));
    }

    public void decreaseUserKeyData(String key, String uid) {
        getDataFromDatabase("Users", uid, database -> checkNull(database, () -> {
            int newLike = Integer.parseInt(String.valueOf(database.get(key))) - 1;
            hashMap = new HashMap<>();
            hashMap.put(key, String.valueOf(newLike));
            pushToDatabase(hashMap, "Users", uid, unused -> {

            }, e -> {

            });
        }));
    }

    public void getDataFromDatabase(String reference, String child, FirebaseDataCallback callback) {
        DatabaseReference usersDatabaseRef = FirebaseDatabase.getInstance().getReference(reference);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && Objects.equals(dataSnapshot.getKey(), child)) {
                    HashMap<String, Object> userData = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Object>>() {
                    });
                    if (userData != null) {
                        callback.onDataRetrieved(userData);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event, if needed
                callback.onDataRetrieved(null);
            }
        };

        usersDatabaseRef.child(child).addListenerForSingleValueEvent(valueEventListener);
    }

    public String getUID() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        return (user != null) ? user.getUid() : "Error";

    }

    public void pushToDatabase(HashMap<String, Object> dataMap, String reference, String child, OnSuccessListener<Void> pushSuccessListener, OnFailureListener pushFailureListener) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(reference);
        databaseReference.child(child).updateChildren(dataMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                pushSuccessListener.onSuccess(null);
            } else {
                pushFailureListener.onFailure(Objects.requireNonNull(task.getException()));
            }
        });
    }

    public void checkNull(Object object, NullChecker nullChecker) {
        if (object != null) {
            nullChecker.notNull();
        }
    }

    public interface FirebaseDataCallback {
        void onDataRetrieved(HashMap<String, Object> database);
    }

    public interface StorageUploadCallback {
        void onSuccess(String url);

        void onFailed(Exception exception);
    }

    public interface NullChecker {
        void notNull();
    }
}
