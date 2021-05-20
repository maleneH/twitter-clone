package dk.cphbusiness.mrv.twitterclone.impl;

import dk.cphbusiness.mrv.twitterclone.contract.UserManagement;
import dk.cphbusiness.mrv.twitterclone.dto.UserCreation;
import dk.cphbusiness.mrv.twitterclone.dto.UserOverview;
import dk.cphbusiness.mrv.twitterclone.dto.UserUpdate;
import dk.cphbusiness.mrv.twitterclone.util.Time;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserManagementImpl implements UserManagement {

    private Jedis jedis;

    public UserManagementImpl(Jedis jedis) {
        this.jedis = jedis;
    }

    @Override
    public boolean createUser(UserCreation userCreation) {

        if (jedis.sismember("users", userCreation.username))
            return false;
        else {
            try (Transaction transaction = jedis.multi()){

                transaction.sadd("users", userCreation.username);
                var details = Map.of("firstname", userCreation.firstname,
                        "lastname", userCreation.lastname,
                        "passwordHash", userCreation.passwordHash,
                        "birthday", userCreation.birthday);

                transaction.hmset("user:"+userCreation.username, details);

                transaction.exec();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
    }


    @Override
    public UserOverview getUserOverview(String username) {
        if (!jedis.exists("user:"+username)) {
            return null;
        } else {
        var user = jedis.hmget("user:"+username, "firstname", "lastname");

        UserOverview userOverview = new UserOverview(
                username,
                user.get(0),
                user.get(1),
                0,
                0);

        return userOverview;
    }
    }

    @Override
    public boolean updateUser(UserUpdate userUpdate) {
        if (!jedis.exists("user:"+userUpdate.username))
            return false;

        if (userUpdate.firstname != null)
            jedis.hset("user:"+userUpdate.username,"firstname", userUpdate.firstname);
        if (userUpdate.lastname != null)
            jedis.hset("user:"+userUpdate.username,"lastname", userUpdate.lastname);
        if (userUpdate.birthday != null)
            jedis.hset("user:"+userUpdate.username,"birthday", userUpdate.birthday);

        return true;

    }

    @Override
    public boolean followUser(String username, String usernameToFollow) {
        if (!jedis.exists(username) || !jedis.exists(usernameToFollow)){
            return false;
        } else {
            jedis.set(username+".follows", usernameToFollow);
            return true;
        }
    }

    @Override
    public boolean unfollowUser(String username, String usernameToUnfollow) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Set<String> getFollowedUsers(String username) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Set<String> getUsersFollowing(String username) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
