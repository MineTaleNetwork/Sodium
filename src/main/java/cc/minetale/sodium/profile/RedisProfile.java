package cc.minetale.sodium.profile;

import cc.minetale.sodium.profile.grant.Grant;
import cc.minetale.sodium.profile.punishment.Punishment;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter @Setter
public class RedisProfile {

    private Profile profile;
    private List<Grant> grants;
    private List<Punishment> punishments;
    private String server;
    private UUID lastMessaged;
    private UUID party;

    public RedisProfile(Profile profile) {
        this.profile = profile;

        grants = profile.getGrants();
        punishments = profile.getPunishments();
    }

    public Profile getProfile() {
        profile.setGrants(grants);
        profile.setPunishments(punishments);

        profile.activateNextGrant();

        return profile;
    }

}
