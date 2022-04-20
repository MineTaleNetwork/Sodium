package cc.minetale.sodium.data;

import cc.minetale.sodium.profile.grant.Grant;
import cc.minetale.sodium.profile.punishment.Punishment;

public class EventListener {

    public void addGrant(Grant grant) {}
    public void removeGrant(Grant grant) {}
    public void expireGrant(Grant grant) {}

    public void addPunishment(Punishment punishment) {}
    public void removePunishment(Punishment punishment) {}
    public void expirePunishment(Punishment punishment) {}

}
