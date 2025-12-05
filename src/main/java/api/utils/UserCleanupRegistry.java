package api.utils;

import api.models.ProfileModel;

public interface UserCleanupRegistry {
    void addUserForCleanup(ProfileModel user);
}