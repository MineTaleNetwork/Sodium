package cc.minetale.sodium;

import cc.minetale.postman.Postman;
import cc.minetale.sodium.data.EventListener;
import cc.minetale.sodium.profile.Profile;
import cc.minetale.sodium.profile.grant.Grant;
import cc.minetale.sodium.profile.punishment.Punishment;
import cc.minetale.sodium.util.Config;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;

public class Sodium {

    @Getter private static final List<EventListener> listeners = new ArrayList<>();
    @Getter private static Gson gson;
    @Getter private static JsonWriterSettings relaxed;
    @Getter private static Postman postman;
    @Getter private static JedisPool jedisPool;
    @Getter private static MongoClient mongoClient;
    @Getter private static MongoDatabase mongoDatabase;
    @Getter private static MiniMessage miniMessage;

    public static void initializeSodium() {
        gson = new GsonBuilder().create();
        relaxed = JsonWriterSettings.builder().outputMode(JsonMode.RELAXED).build();

        miniMessage = MiniMessage.builder()
                .tags(TagResolver.builder()
                        .resolver(TagResolver.resolver(
                                TagResolver.standard()
                        )).build())
                .build();

        loadPostman();
        loadMongo();
        loadRedis();
    }

    private static void loadPostman() {
        postman = new Postman(gson);

        postman.getPayloadsRegistry().registerPayloadsInPackage("cc.minetale.sodium.payloads");
    }

    private static void loadRedis() {
        jedisPool = new JedisPool(Config.REDIS_HOST, Config.REDIS_PORT);
    }

    private static void loadMongo() {
        mongoClient = new MongoClient(Config.MONGO_HOST, Config.MONGO_PORT);
        mongoDatabase = mongoClient.getDatabase(Config.MONGO_DATABASE);

        Profile.setCollection(mongoDatabase.getCollection("profiles"));
        Punishment.setCollection(mongoDatabase.getCollection("punishments"));
        Grant.setCollection(mongoDatabase.getCollection("grants"));
    }

}
