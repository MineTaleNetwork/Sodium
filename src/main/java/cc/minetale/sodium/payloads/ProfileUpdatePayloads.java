package cc.minetale.sodium.payloads;

import cc.minetale.postman.payload.Payload;
import cc.minetale.sodium.profile.grant.Grant;
import cc.minetale.sodium.profile.punishment.Punishment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

public final class ProfileUpdatePayloads {

    private ProfileUpdatePayloads() {}

    @Getter @AllArgsConstructor
    public static class UpdateProfilePayload extends Payload {

        private UUID playerUuid;

    }

    @Getter @AllArgsConstructor
    public static class GrantPayload extends Payload {

        private UUID playerUuid;
        private Action action;
        private Grant grant;

    }

    @Getter @AllArgsConstructor
    public static class PunishmentPayload extends Payload {

        private UUID playerUuid;
        private Action action;
        private Punishment punishment;

    }

    public enum Action {
        ADD, REMOVE, EXPIRE
    }

}
