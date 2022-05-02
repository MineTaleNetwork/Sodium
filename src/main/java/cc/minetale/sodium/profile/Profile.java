package cc.minetale.sodium.profile;

import cc.minetale.postman.Postman;
import cc.minetale.sodium.Sodium;
import cc.minetale.sodium.cache.ProfileCache;
import cc.minetale.sodium.data.HubVisibility;
import cc.minetale.sodium.payloads.ProfileUpdatePayloads;
import cc.minetale.sodium.profile.punishment.PunishmentType;
import cc.minetale.sodium.util.MongoUtil;
import cc.minetale.sodium.profile.grant.Grant;
import cc.minetale.sodium.profile.punishment.Punishment;
import com.google.gson.annotations.SerializedName;
import com.mongodb.client.MongoCollection;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;

@Getter @Setter
public class Profile {

    @Setter @Getter private static MongoCollection<Document> collection;

    @SerializedName("_id")
    private UUID uuid;
    private String username;
    private String currentAddress;
    private String discord;
    private int gold;
    private long firstSeen;
    private long lastSeen;
    private long experience;
    private List<UUID> ignored = new ArrayList<>();
    private List<UUID> friends = new ArrayList<>();
    private Options optionsProfile = new Options();
    private Staff staffProfile = new Staff();
    private Hub hubProfile = new Hub();

    private transient Grant grant = Grant.DEFAULT_GRANT;
    private transient List<Punishment> punishments = new ArrayList<>();
    private transient List<Grant> grants = new ArrayList<>();

    public Profile(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    public Profile(UUID uuid) {
        this(uuid, null);
    }

    public boolean isIgnoring(Profile profile) {
        return this.ignored.contains(profile.getUuid());
    }

    public boolean isStaff() {
        return grant.getRank().isStaff();
    }

    public boolean isFriends(Profile profile) {
        return friends.contains(profile.getUuid());
    }

    public void addFriend(Profile profile) {
        friends.add(profile.getUuid());
    }

    public void removeFriend(Profile profile) {
        friends.remove(profile.getUuid());
    }

    public int getPunishmentCountByType(PunishmentType type) {
        return (int) this.punishments.stream().filter(punishment -> punishment.getType() == type).count();
    }

    public Punishment getActivePunishmentByType(PunishmentType type) {
        for (var punishment : this.punishments) {
            if (punishment.getType() == type && punishment.isActive()) {
                return punishment;
            }
        }

        return null;
    }

    public void expirePunishments() {
        for (var punishment : punishments) {
            if (!punishment.isRemoved() && punishment.hasExpired()) {
                expirePunishment(punishment);
            }
        }
    }

    public void issuePunishment(Punishment punishment) {
        punishments.add(punishment);

        MongoUtil.saveDocument(Punishment.getCollection(), punishment.getUuid(), punishment);
        ProfileCache.modifyPunishment(getUuid(), punishment);

        Postman.getPostman().broadcast(new ProfileUpdatePayloads.PunishmentPayload(uuid, ProfileUpdatePayloads.Action.ADD, punishment));

        for (var provider : Sodium.getListeners()) {
            provider.addPunishment(punishment);
        }
    }

    public void removePunishment(Punishment punishment, @Nullable UUID removedBy, long removedAt, String removedReason) {
        punishment.setRemovedById(removedBy);
        punishment.setRemovedAt(removedAt);
        punishment.setRemovedReason(removedReason);

        MongoUtil.saveDocument(Punishment.getCollection(), punishment.getUuid(), punishment);
        ProfileCache.modifyPunishment(getUuid(), punishment);

        Postman.getPostman().broadcast(new ProfileUpdatePayloads.PunishmentPayload(uuid, ProfileUpdatePayloads.Action.REMOVE, punishment));

        for (var provider : Sodium.getListeners()) {
            provider.removePunishment(punishment);
        }
    }

    public void expirePunishment(Punishment punishment) {
        punishment.setRemovedById(null);
        punishment.setRemovedAt(punishment.getAddedAt() + punishment.getDuration());
        punishment.setRemovedReason("Punishment Expired");

        MongoUtil.saveDocument(Punishment.getCollection(), punishment.getUuid(), punishment);
        ProfileCache.modifyPunishment(uuid, punishment);

        Postman.getPostman().broadcast(new ProfileUpdatePayloads.PunishmentPayload(uuid, ProfileUpdatePayloads.Action.EXPIRE, punishment));

        for (var provider : Sodium.getListeners()) {
            provider.expirePunishment(punishment);
        }
    }

    public void issueGrant(Grant grant) {
        if (grant.isDefault()) { return; }

        grants.add(grant);

        MongoUtil.saveDocument(Grant.getCollection(), grant.getUuid(), grant);
        ProfileCache.modifyGrant(uuid, grant);

        Postman.getPostman().broadcast(new ProfileUpdatePayloads.GrantPayload(uuid, ProfileUpdatePayloads.Action.ADD, grant));

        for (var listener : Sodium.getListeners()) {
            listener.addGrant(grant);
        }
    }

    public void removeGrant(Grant grant, @Nullable UUID removedBy, long removedAt, String removedReason) {
        if (grant.isDefault()) { return; }

        grant.setRemovedById(removedBy);
        grant.setRemovedAt(removedAt);
        grant.setRemovedReason(removedReason);

        MongoUtil.saveDocument(Grant.getCollection(), grant.getUuid(), grant);
        ProfileCache.modifyGrant(uuid, grant);

        Postman.getPostman().broadcast(new ProfileUpdatePayloads.GrantPayload(uuid, ProfileUpdatePayloads.Action.REMOVE, grant));

        for (var listener : Sodium.getListeners()) {
            listener.removeGrant(grant);
        }
    }

    public void expireGrant(Grant grant) {
        if (grant.isDefault()) { return; }

        grant.setRemovedById(null);
        grant.setRemovedAt(grant.getAddedAt() + grant.getDuration());
        grant.setRemovedReason("Grant Expired");

        MongoUtil.saveDocument(Grant.getCollection(), grant.getUuid(), grant);
        ProfileCache.modifyGrant(uuid, grant);

        Postman.getPostman().broadcast(new ProfileUpdatePayloads.GrantPayload(uuid, ProfileUpdatePayloads.Action.EXPIRE, grant));

        for (var listener : Sodium.getListeners()) {
            listener.expireGrant(grant);
        }
    }

    public void checkGrants() {
        for (var grant : grants) {
            if (!grant.isRemoved() && grant.hasExpired()) {
                expireGrant(grant);
            }
        }

        activateNextGrant();
    }

    public void activateNextGrant() {
        var activeGrants = new ConcurrentSkipListMap<Integer, Grant>();

        for (var grant : grants) {
            if (grant.isActive()) {
                activeGrants.put(grant.getRank().ordinal(), grant);
            }
        }

        var grantEntry = activeGrants.firstEntry();

        if (grantEntry != null) {
            this.grant = grantEntry.getValue();
        } else {
            this.grant = Grant.DEFAULT_GRANT;
        }
    }

    public List<Grant> getSortedGrants() {
        var activeGrants = new ArrayList<>(grants);

        var sorter = (Comparator<Grant>) (grant1, grant2) -> {
            var activeCompare = Boolean.compare(grant2.isActive(), grant1.isActive());

            if (activeCompare != 0) {
                return activeCompare;
            }

            var rank1 = grant1.getRank();
            var rank2 = grant2.getRank();

            return Integer.compare(rank1.ordinal(), rank2.ordinal());
        };

        activeGrants.sort(sorter);

        return activeGrants;
    }

    public Component getChatFormat() {
        return Component.text().append(
                getColoredPrefix(),
                Component.space(),
                getColoredName()
        ).build();
    }

    public Component getColoredName() {
        return Component.text(username, grant.getRank().getColor());
    }

    public Component getColoredPrefix() {
        return grant.getRank().getPrefix();
    }

    @Getter @Setter
    public static class Options {
        private boolean receivingPartyRequests = true;
        private boolean receivingFriendRequests = true;
        private boolean receivingPublicChat = true;
        private boolean receivingConversations = true;
        private boolean receivingMessageSounds = true;
    }

    @Getter @Setter
    public static class Staff {
        private String twoFactorKey = "";
        private boolean receivingStaffMessages = true;
        private boolean locked = false;
    }

    @Getter @Setter
    public static class Hub {
        private HubVisibility visibility = HubVisibility.ALL;
    }

}
