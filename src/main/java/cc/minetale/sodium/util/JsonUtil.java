package cc.minetale.sodium.util;

import cc.minetale.sodium.Sodium;
import lombok.experimental.UtilityClass;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

@UtilityClass
public class JsonUtil {

    private static final JsonWriterSettings relaxed = JsonWriterSettings.builder().outputMode(JsonMode.RELAXED).build();

    public static String writeToJson(Object object) {
        return Sodium.getGson().toJson(object);
    }

    public static <T> T readFromJson(String json, Class<T> clazz) {
        return Sodium.getGson().fromJson(json, clazz);
    }

    public static <T> T readFromJson(Document document, Class<T> clazz) {
        return Sodium.getGson().fromJson(document.toJson(relaxed), clazz);
    }

}
