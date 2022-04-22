package cc.minetale.sodium.payloads;

import cc.minetale.postman.payload.Payload;
import cc.minetale.sodium.profile.punishment.Punishment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter @AllArgsConstructor
public class PunishmentAddPayload extends Payload {

    private UUID player;
    private Punishment punishment;

}
