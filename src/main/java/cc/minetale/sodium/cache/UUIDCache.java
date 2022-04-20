package cc.minetale.sodium.cache;

import cc.minetale.sodium.util.Config;
import cc.minetale.sodium.util.Redis;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class UUIDCache {

    public static final String NAME_TO_UUID = Config.CACHE_PREFIX + ":name-to-uuid";
    public static final String UUID_TO_NAME = Config.CACHE_PREFIX + ":uuid-to-name";

    public static String getName(UUID uuid) {
        return Redis.runRedisCommand(jedis -> jedis.hget(UUID_TO_NAME, uuid.toString()));
    }

    public static UUID getUuid(String name) {
        String uuid = Redis.runRedisCommand(jedis -> jedis.hget(NAME_TO_UUID, name.toUpperCase()));

        if(uuid != null) {
            return UUID.fromString(uuid);
        }

        return null;
    }

    public static void pushCache(UUID uuid, String name) {
        Redis.runRedisCommand(jedis -> {
            var oldName = jedis.hget(
                    UUID_TO_NAME,
                    uuid.toString()
            );

            var pipeline = jedis.pipelined();

            if(oldName != null && !oldName.equalsIgnoreCase(name)) {
                pipeline.hdel(NAME_TO_UUID, oldName);
            }

            pipeline.hset(
                    NAME_TO_UUID,
                    name.toUpperCase(),
                    uuid.toString()
            );

            pipeline.hset(
                    UUID_TO_NAME,
                    uuid.toString(),
                    name.toUpperCase()
            );

            pipeline.sync();

            return null;
        });
    }

}
