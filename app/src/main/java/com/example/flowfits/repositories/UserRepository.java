package com.example.flowfits.repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import com.example.flowfits.firebase.FirebaseManager;
import com.example.flowfits.models.User;
import com.example.flowfits.utils.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class UserRepository {
    private static UserRepository instance;
    private DatabaseReference databaseRef;
    private FirebaseManager firebaseManager;
    private MutableLiveData<User> currentUser;

    public UserRepository() {
        firebaseManager = FirebaseManager.getInstance();
        databaseRef = firebaseManager.getDatabaseReference();
        currentUser = new MutableLiveData<>();
    }

    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    public MutableLiveData<User> getCurrentUser() {
        return currentUser;
    }

    public void loadCurrentUser() {
        String userId = firebaseManager.getCurrentUserId();
        if (userId != null) {
            loadUser(userId);
        }
    }

    public void loadUser(String userId) {
        databaseRef.child(Constants.USERS_COLLECTION)
                .child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            user.setUserId(dataSnapshot.getKey());
                        }
                        currentUser.setValue(user);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                        currentUser.setValue(null);
                    }
                });
    }

    public void createUser(User user, UserCallback callback) {
        if (user.getUserId() == null) {
            String userId = firebaseManager.getCurrentUserId();
            if (userId == null) {
                if (callback != null) {
                    callback.onFailure("User not logged in");
                }
                return;
            }
            user.setUserId(userId);
        }

        databaseRef.child(Constants.USERS_COLLECTION)
                .child(user.getUserId())
                .setValue(user)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess(user);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    public void updateUser(User user, UserUpdateCallback callback) {
        if (user.getUserId() == null) {
            if (callback != null) {
                callback.onFailure("User ID is required for update");
            }
            return;
        }

        databaseRef.child(Constants.USERS_COLLECTION)
                .child(user.getUserId())
                .setValue(user)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    public void getUserById(String userId, UserCallback callback) {
        databaseRef.child(Constants.USERS_COLLECTION)
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            user.setUserId(dataSnapshot.getKey());
                            if (callback != null) {
                                callback.onSuccess(user);
                            }
                        } else {
                            if (callback != null) {
                                callback.onFailure("User not found");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        if (callback != null) {
                            callback.onFailure(databaseError.getMessage());
                        }
                    }
                });
    }

    public void updateUserStats(String userId, User.UserStats stats, OnUserUpdatedListener listener) {
        // Implementation for updating user stats using Firebase Realtime Database
    }

    public void updateUserDisplayName(String userId, String displayName, UserCallback callback) {
        databaseRef.child(Constants.USERS_COLLECTION)
                .child(userId)
                .child("displayName")
                .setValue(displayName)
                .addOnSuccessListener(aVoid -> {
                    // Get the updated user and return it
                    getUserById(userId, callback);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    public void updateUserProfilePicture(String userId, String profilePictureUrl, UserCallback callback) {
        databaseRef.child(Constants.USERS_COLLECTION)
                .child(userId)
                .child("profilePictureUrl")
                .setValue(profilePictureUrl)
                .addOnSuccessListener(aVoid -> {
                    // Get the updated user and return it
                    getUserById(userId, callback);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    // Callback interfaces
    public interface UserCallback {
        void onSuccess(User user);
        void onFailure(String error);
    }

    public interface UserUpdateCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // Interfaces for callbacks
    public interface OnUserUpdatedListener {
        void onSuccess();
        void onFailure(String error);
    }
} 