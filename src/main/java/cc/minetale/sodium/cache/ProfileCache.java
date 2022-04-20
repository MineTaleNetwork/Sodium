package cc.minetale.sodium.cache;

import cc.minetale.postman.Postman;
import cc.minetale.sodium.payloads.UpdateProfilePayload;
import cc.minetale.sodium.profile.Profile;
import cc.minetale.sodium.profile.ProfileUtil;
import cc.minetale.sodium.profile.RedisProfile;
import cc.minetale.sodium.util.Config;
import cc.minetale.sodium.util.JsonUtil;
import cc.minetale.sodium.util.Redis;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class ProfileCache {

    public static final String KEY = Config.CACHE_PREFIX + ":profile-cache";

    public static void updateParty(UUID player, UUID party) {
        var redisProfile = ProfileUtil.fromCache(player);

        if (redisProfile != null) {
            redisProfile.setParty(party);

            pushCache(redisProfile);
        }
    }

    public static void updateLastMessaged(UUID player, UUID lastMessaged) {
        var redisProfile = ProfileUtil.fromCache(player);

        if (redisProfile != null) {
            redisProfile.setLastMessaged(lastMessaged);

            pushCache(redisProfile);
        }
    }

    public static void updateStatus(UUID uuid, String server) {
        var redisProfile = ProfileUtil.fromCache(uuid);

        if (redisProfile != null) {
            redisProfile.setServer(server);

            pushCache(redisProfile);
        }
    }

    public static void updateProfile(Profile profile) {
        var redisProfile = ProfileUtil.fromCache(profile.getUuid());

        if (redisProfile != null) {
            redisProfile.setProfile(profile);
            redisProfile.setGrants(profile.getGrants());
            redisProfile.setPunishments(profile.getPunishments());

            pushCache(redisProfile);

            Postman.getPostman().broadcast(new UpdateProfilePayload(profile.getUuid()));
        }
    }

    public static void pushCache(Profile profile) {
        pushCache(new RedisProfile(profile));
    }

    public static void pushCache(RedisProfile profile) {
        var uuid = profile.getProfile().getUuid().toString();
        var json = JsonUtil.writeToJson(profile);

        if (json == null) {
            return;
        }

        Redis.runRedisCommand(jedis -> jedis.hset(
                KEY,
                uuid,
                json
        ));

        Redis.expireMember(KEY, uuid, 12 * 60 * 60);
    }

}
