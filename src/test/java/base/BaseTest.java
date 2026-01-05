package base;

import api.models.ProfileModel;
import api.requests.steps.AdminSteps;
import api.utils.UserCleanupRegistry;
import common.extensions.TimingExtension;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(TimingExtension.class)
public class BaseTest implements UserCleanupRegistry {
    protected SoftAssertions softly;
    protected static final ThreadLocal<List<ProfileModel>> usersToCleanup = ThreadLocal.withInitial(ArrayList::new);

    @BeforeEach
    public void setupTest() {
        this.softly = new SoftAssertions();
        usersToCleanup.get().clear();
    }

    @AfterEach
    public void afterTest() {
        List<ProfileModel> users = usersToCleanup.get();
        for (ProfileModel user : users) {
            try {
                AdminSteps.deleteUser(user);
            } catch (Exception e) {
                System.err.println("Failed to delete user: " + e.getMessage());
            }
        }
        usersToCleanup.get().clear();
        softly.assertAll();
    }

    @Override
    public void addUserForCleanup(ProfileModel user) {
        usersToCleanup.get().add(user);
    }

}
