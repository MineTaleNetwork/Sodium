package cc.minetale.sodium.profile;

import cc.minetale.sodium.cache.ProfileCache;
import cc.minetale.sodium.cache.UUIDCache;
import cc.minetale.sodium.profile.grant.Grant;
import cc.minetale.sodium.profile.punishment.Punishment;
import cc.minetale.sodium.util.JsonUtil;
import cc.minetale.sodium.util.Redis;
import com.mongodb.client.model.Filters;
import lombok.experimental.UtilityClass;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@UtilityClass
public class ProfileUtil {

    public static Profile retrieveProfile(UUID uuid, String name) {
        var response = fromDatabase(uuid);

        UUIDCache.pushCache(uuid, name);

        switch (response.response()) {
            case RETRIEVED -> {
                var profile = response.profile();

                ProfileCache.pushCache(profile);

                return profile;
            }
            case NOT_FOUND -> {
                var profile = new Profile(uuid, name);

                ProfileCache.pushCache(profile);

                return profile;
            }
        }

        return null;
    }

    public static Profile getProfile(UUID uuid) {
        var redisProfile = fromCache(uuid);
        if (redisProfile != null) {
            return redisProfile.getProfile();
        }

        var response = fromDatabase(uuid);
        if (response.response() == ProfileRetrieval.Response.RETRIEVED) {
            return response.profile();
        }

        return null;
    }

    public static Profile getProfile(String username) {
        var uuid = UUIDCache.getUuid(username);
        if (uuid != null) {
            return getProfile(uuid);
        }

        return null;
    }

    public static ProfileRetrieval fromDocument(Document document) {
        if (document == null) {
            return ProfileRetrieval.NOT_FOUND;
        }

        var profile = JsonUtil.readFromJson(document, Profile.class);
        if (profile == null) {
            return ProfileRetrieval.FAILED;
        }

        var uuid = profile.getUuid();

        profile.setGrants(Grant.getGrants(uuid));
        profile.setPunishments(Punishment.getPunishments(uuid));

        return new ProfileRetrieval(ProfileRetrieval.Response.RETRIEVED, profile);
    }

    public static ProfileRetrieval fromDatabase(UUID uuid) {
        var document = Profile.getCollection().find(Filters.eq("_id", uuid.toString())).first();

        return fromDocument(document);
    }

    public static RedisProfile fromCache(UUID uuid) {
        var json = Redis.runRedisCommand(jedis -> jedis.hget(ProfileCache.KEY, uuid.toString()));

        if (json == null) { return null; }

        return JsonUtil.readFromJson(json, RedisProfile.class);
    }

    public static RedisProfile fromCache(String username) {
        var uuid = UUIDCache.getUuid(username);

        if (uuid != null) { return fromCache(uuid); }

        return null;
    }

    public static CompletableFuture<List<RedisProfile>> getProfiles(List<UUID> players) {
        return new CompletableFuture<List<RedisProfile>>().completeAsync(() -> {
            if (players.isEmpty()) { return Collections.emptyList(); }

            var profiles = new ArrayList<RedisProfile>();

            var redisProfiles = Redis.runRedisCommand(jedis -> jedis.hmget(ProfileCache.KEY,
                    players.stream()
                            .map(UUID::toString)
                            .toArray(String[]::new)
            ));

            var uuidQueue = new ArrayList<UUID>();

            if(redisProfiles != null) {
                for(int i = 0; i < players.size(); i++) {
                    var json = redisProfiles.get(i);

                    if(json == null) {
                        uuidQueue.add(players.get(i));
                    } else {
                        var redisProfile = JsonUtil.readFromJson(json, RedisProfile.class);
                        if(redisProfile != null) {
                            profiles.add(redisProfile);
                        } else {
                            uuidQueue.add(players.get(i));
                        }
                    }
                }
            }

            var mongoProfiles = new ArrayList<RedisProfile>();

            if(!uuidQueue.isEmpty()) {
                var documents = Profile.getCollection()
                        .find(Filters.in("_id", uuidQueue.stream()
                                .map(UUID::toString)
                                .collect(Collectors.toList()))
                        );

                for (var document : documents) {
                    var response = fromDocument(document);

                    if (response.response() == ProfileRetrieval.Response.RETRIEVED) {
                        mongoProfiles.add(new RedisProfile(response.profile()));
                    }
                }
            }

            ProfileCache.bulkPushCache(mongoProfiles);
            profiles.addAll(mongoProfiles);

            return profiles;
        });
    }

}
