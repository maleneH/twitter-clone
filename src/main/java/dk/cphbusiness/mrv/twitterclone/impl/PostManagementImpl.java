package dk.cphbusiness.mrv.twitterclone.impl;

import dk.cphbusiness.mrv.twitterclone.contract.PostManagement;
import dk.cphbusiness.mrv.twitterclone.dto.Post;
import dk.cphbusiness.mrv.twitterclone.util.Time;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PostManagementImpl implements PostManagement {
    private Jedis jedis;
    private Time time;

    public PostManagementImpl(Jedis jedis, Time time) {
        this.jedis = jedis;
        this.time = time;
    }

    @Override
    public boolean createPost(String username, String message) {
        if (!jedis.sismember("users", username)) {return false;}
        else {
            try (Transaction transaction = jedis.multi()) {
                long ts = time.getCurrentTimeMillis();
                transaction.sadd("posts:" + username, message);
                transaction.hset("post:" + username, String.valueOf(ts), message);
                transaction.exec();
                return true;
            }
        }
    }

    @Override
    public List<Post> getPosts(String username) {
        var messages = jedis.hgetAll("post:"+username);
        List<Post> posts = new ArrayList<>();
        for (String tm : messages.keySet())
        {
            posts.add(new Post(Long.parseLong(tm), messages.get(tm)));
        }
        return posts;
    }

    @Override
    public List<Post> getPostsBetween(String username, long timeFrom, long timeTo) {
        var messages = jedis.hgetAll("post:"+username);
        List<Post> posts = new ArrayList<>();
        for (String tm : messages.keySet())
        {
            if (Long.parseLong(tm) >= timeFrom && Long.parseLong(tm) <= timeTo) {
                posts.add(new Post(Long.parseLong(tm), messages.get(tm)));
            }
        }
        return posts;
    }
}
