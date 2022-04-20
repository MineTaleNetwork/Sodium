package cc.minetale.sodium.payloads;

import cc.minetale.postman.payload.Payload;
import cc.minetale.sodium.profile.punishment.Punishment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter @AllArgsConstructor
public class PunishmentPayload extends Payload {

    private UUID player;
    private Action action;
    private Punishment punishment;

    public enum Action {
        ADD,
        REMOVE,
        EXPIRE
    }

}
