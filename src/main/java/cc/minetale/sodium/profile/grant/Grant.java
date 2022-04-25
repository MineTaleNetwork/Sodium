package cc.minetale.sodium.profile.grant;

import cc.minetale.sodium.data.ProfileObject;
import cc.minetale.sodium.util.JsonUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter @Setter
public class Grant extends ProfileObject {

    @Setter @Getter private static MongoCollection<Document> collection;

    private Rank rank;

    public static final Grant DEFAULT_GRANT = new Grant(
            "DEFAULT",
            null,
            null,
            0L,
            "Default",
            Integer.MAX_VALUE,
            Rank.MEMBER
    );

    public Grant(
            @NotNull String uuid,
            UUID playerId,
            UUID addedById,
            long addedAt,
            String addedReason,
            long duration,
            Rank rank
    ) {
        super(uuid, playerId, addedById, addedAt, addedReason, duration);

        this.rank = rank;
    }

    public boolean isDefault() {
        return rank == Rank.MEMBER;
    }

    public static Grant getGrant(String grant) {
        var document = collection.find(Filters.eq("_id", grant)).first();

        if (document != null) {
            return JsonUtil.readFromJson(document, Grant.class);
        }

        return null;
    }

    public static List<Grant> getGrants(UUID player) {
        var grants = new ArrayList<>(List.of(Grant.DEFAULT_GRANT));

        for (var document : collection.find(Filters.eq("playerId", player.toString()))) {
            var grant = JsonUtil.readFromJson(document, Grant.class);

            if(grant != null) {
                grants.add(grant);
            }
        }

        return grants;
    }

}