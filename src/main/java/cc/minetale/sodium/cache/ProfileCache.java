package cc.minetale.sodium.cache;

import cc.minetale.sodium.profile.Profile;
import cc.minetale.sodium.profile.ProfileUtil;
import cc.minetale.sodium.profile.RedisProfile;
import cc.minetale.sodium.profile.grant.Grant;
import cc.minetale.sodium.profile.punishment.Punishment;
import cc.minetale.sodium.util.Config;
import cc.minetale.sodium.util.JsonUtil;
import cc.minetale.sodium.util.Redis;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.UUID;

@UtilityClass
public class ProfileCache {

    public static final String KEY = Config.CACHE_PREFIX + ":profile-cache";

    public static void updateParty(UUID player, UUID party) {
        var redisProfile = ProfileUtil.fromCache(player);

        if (redisProfile == null) { return; }

        redisProfile.setParty(party);

        pushCache(redisProfile);
    }

    public static void updateLastMessaged(UUID player, UUID lastMessaged) {
        var redisProfile = ProfileUtil.fromCache(player);

        if (redisProfile == null) { return; }

        redisProfile.setLastMessaged(lastMessaged);

        pushCache(redisProfile);
    }

    public static void updateStatus(UUID player, String server) {
        var redisProfile = ProfileUtil.fromCache(player);

        if (redisProfile == null) { return; }

        redisProfile.setServer(server);

        pushCache(redisProfile);
    }

    public static void modifyGrant(UUID player, Grant grant) {
        var redisProfile = ProfileUtil.fromCache(player);

        if (redisProfile == null) { return; }

        var grants = redisProfile.getGrants();

        grants.remove(grant);
        grants.add(grant);

        pushCache(redisProfile);
    }

    public static void modifyPunishment(UUID player, Punishment punishment) {
        var redisProfile = ProfileUtil.fromCache(player);

        if (redisProfile == null) { return; }

        var punishments = redisProfile.getPunishments();

        punishments.remove(punishment);
        punishments.add(punishment);

        pushCache(redisProfile);
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

    public static void bulkPushCache(List<RedisProfile> profiles) {
        Redis.runRedisCommand(redis -> {
            var pipeline = redis.pipelined();

            for(var profile : profiles) {
                var uuid = profile.getProfile().getUuid().toString();
                var json = JsonUtil.writeToJson(profile);

                if (json == null) { continue; }

                pipeline.hset(
                        KEY,
                        uuid,
                        json
                );

                pipeline.sendCommand(Redis.CustomCommand.EXPIREMEMBER, KEY, uuid, String.valueOf(12 * 60 * 60));
            }

            pipeline.sync();

            return null;
        });
    }

}
