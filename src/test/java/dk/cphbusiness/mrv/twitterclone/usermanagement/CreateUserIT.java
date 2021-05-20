package dk.cphbusiness.mrv.twitterclone.usermanagement;

import dk.cphbusiness.mrv.twitterclone.TestBase;
import dk.cphbusiness.mrv.twitterclone.dto.UserCreation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CreateUserIT extends TestBase {
    @Test
    public void createUserMustReturnFalseIfUserExists(){
        // Arrange
        UserCreation uc = getAlbert();
        boolean res = um.createUser(uc);
        assertTrue(res);

        // Act
        res = um.createUser(uc);

        // Assert
        assertFalse(res);
    }

    @Test
    public void userMustExistWhenCreated(){
        // Arrange
        var user = getAlbert();
        var user2 = getBenny();
        um.createUser(user);
        //useroverview object requires the user to have followers and to follow
        um.followUser(user.username, user2.username);
        um.followUser(user2.username, user.username);

        // Act
        var userOverview = um.getUserOverview(user.username);

        // Assert
        assertEquals(user.firstname, userOverview.firstname);
        assertEquals(user.lastname, userOverview.lastname);
    }

}
