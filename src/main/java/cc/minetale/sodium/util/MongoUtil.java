package cc.minetale.sodium.util;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import lombok.experimental.UtilityClass;
import org.bson.Document;

@UtilityClass
public class MongoUtil {

    public static void saveDocument(MongoCollection<Document> collection, Object uuid, Object object) {
        var document = JsonUtil.writeToJson(object);

        if (document != null) {
            collection.replaceOne(
                    Filters.eq(uuid.toString()),
                    Document.parse(document),
                    new ReplaceOptions().upsert(true)
            );
        }
    }

    public static void deleteDocument(MongoCollection<Document> collection, Object uuid) {
        collection.deleteOne(Filters.eq(uuid.toString()));
    }

}
