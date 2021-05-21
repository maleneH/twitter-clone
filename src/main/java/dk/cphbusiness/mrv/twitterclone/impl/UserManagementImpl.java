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
        if (!jedis.exists("user:"+userUpdate.username)) {
            return false;
        }
        else {
            if (userUpdate.firstname != null)
                jedis.hset("user:" + userUpdate.username, "firstname", userUpdate.firstname);
            if (userUpdate.lastname != null)
                jedis.hset("user:" + userUpdate.username, "lastname", userUpdate.lastname);
            if (userUpdate.birthday != null)
                jedis.hset("user:" + userUpdate.username, "birthday", userUpdate.birthday);

            return true;
        }
    }

    @Override
    public boolean followUser(String username, String usernameToFollow) {
        if (!jedis.exists("user:" + username) || !jedis.exists("user:" + usernameToFollow)){
            return false;
        } else {
            jedis.sadd("following:" + username, usernameToFollow);
            jedis.sadd("followers:" + usernameToFollow, username);

            return true;
        }

    }

    @Override
    public boolean unfollowUser(String username, String usernameToUnfollow) {
        if (!jedis.exists("user:" + username) || !jedis.exists("user:" + usernameToUnfollow)) {
            return false;
        }else{
            jedis.srem("following:"+username, usernameToUnfollow);
            jedis.srem("followers:"+usernameToUnfollow, username);
            return true;
        }
    }

    @Override
    public Set<String> getFollowedUsers(String username) {
        if (jedis.exists("user:"+username)) {
            var followedUsers = jedis.smembers("followers:" + username);
            return followedUsers;
        }else{
            return null;
        }

    }

    @Override
    public Set<String> getUsersFollowing(String username) {
        if (jedis.exists("user:"+username)) {
            var followingUsers = jedis.smembers("followers:" + username);
            return followingUsers;
        }else{
            return null;
        }
    }

}
