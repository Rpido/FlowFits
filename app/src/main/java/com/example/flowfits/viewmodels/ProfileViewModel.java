package com.example.flowfits.viewmodels;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flowfits.models.User;
import com.example.flowfits.repositories.AnalyticsRepository;
import com.example.flowfits.repositories.UserRepository;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final AnalyticsRepository analyticsRepository;
    private final FirebaseAuth firebaseAuth;
    private final FirebaseStorage firebaseStorage;

    // LiveData for UI
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<User.UserStats> userStats = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();

    public ProfileViewModel() {
        userRepository = UserRepository.getInstance();
        analyticsRepository = AnalyticsRepository.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
    }

    // Getters for LiveData
    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<User.UserStats> getUserStats() {
        return userStats;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    public void loadCurrentUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            userRepository.getUserById(firebaseUser.getUid(), new UserRepository.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    currentUser.postValue(user);
                }

                @Override
                public void onFailure(String error) {
                    errorMessage.postValue(error);
                }
            });
        }
    }

    public void loadUserStats() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            analyticsRepository.getUserStats(firebaseUser.getUid(), new AnalyticsRepository.UserStatsCallback() {
                @Override
                public void onSuccess(User.UserStats stats) {
                    userStats.postValue(stats);
                }

                @Override
                public void onFailure(String error) {
                    errorMessage.postValue("Failed to load statistics: " + error);
                }
            });
        }
    }

    public void updateDisplayName(String newDisplayName) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            isLoading.setValue(true);
            
            userRepository.updateUserDisplayName(firebaseUser.getUid(), newDisplayName, new UserRepository.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    isLoading.postValue(false);
                    currentUser.postValue(user);
                    successMessage.postValue("Display name updated successfully");
                }

                @Override
                public void onFailure(String error) {
                    isLoading.postValue(false);
                    errorMessage.postValue("Failed to update display name: " + error);
                }
            });
        }
    }

    public void changePassword(String currentPassword, String newPassword) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null || firebaseUser.getEmail() == null) {
            errorMessage.setValue("User not authenticated");
            return;
        }

        isLoading.setValue(true);

        // Re-authenticate user before changing password
        AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), currentPassword);
        
        firebaseUser.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    // Re-authentication successful, now update password
                    firebaseUser.updatePassword(newPassword)
                            .addOnSuccessListener(aVoid1 -> {
                                isLoading.postValue(false);
                                successMessage.postValue("Password changed successfully");
                            })
                            .addOnFailureListener(e -> {
                                isLoading.postValue(false);
                                errorMessage.postValue("Failed to change password: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    isLoading.postValue(false);
                    errorMessage.postValue("Current password is incorrect");
                });
    }

    public void uploadProfilePicture(Uri imageUri) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            errorMessage.setValue("User not authenticated");
            return;
        }

        isLoading.setValue(true);

        // Create a reference to store the image
        StorageReference profileImageRef = firebaseStorage.getReference()
                .child("profile_pictures")
                .child(firebaseUser.getUid() + ".jpg");

        // Upload the image
        profileImageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL
                    profileImageRef.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                // Update user profile with new image URL
                                userRepository.updateUserProfilePicture(firebaseUser.getUid(), 
                                    downloadUri.toString(), new UserRepository.UserCallback() {
                                    @Override
                                    public void onSuccess(User user) {
                                        isLoading.postValue(false);
                                        currentUser.postValue(user);
                                        successMessage.postValue("Profile picture updated successfully");
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        isLoading.postValue(false);
                                        errorMessage.postValue("Failed to update profile picture: " + error);
                                    }
                                });
                            })
                            .addOnFailureListener(e -> {
                                isLoading.postValue(false);
                                errorMessage.postValue("Failed to get image URL: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    isLoading.postValue(false);
                    errorMessage.postValue("Failed to upload image: " + e.getMessage());
                });
    }

    public void logout() {
        firebaseAuth.signOut();
    }
} 