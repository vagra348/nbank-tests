package utils;

import models.ProfileModel;

public interface UserCleanupRegistry {
    void addUserForCleanup(ProfileModel user);
}