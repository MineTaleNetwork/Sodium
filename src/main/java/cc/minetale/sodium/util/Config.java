package cc.minetale.sodium.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Config {

    // Cache
    public static final String CACHE_PREFIX = System.getProperty("cachePrefix", "minetale");

    // Mongo
    public static final String MONGO_HOST = System.getProperty("mongoHost", "127.0.0.1");
    public static final int MONGO_PORT = Integer.getInteger("mongoPort", 27017);
    public static String MONGO_DATABASE = System.getProperty("mongoDatabase", "MineTale");

    // Redis
    public static final String REDIS_HOST = System.getProperty("redisHost", "127.0.0.1");
    public static final int REDIS_PORT = Integer.getInteger("redisPort", 6379);

}

