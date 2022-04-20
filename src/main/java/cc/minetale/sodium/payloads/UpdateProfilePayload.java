package cc.minetale.sodium.payloads;

import cc.minetale.postman.payload.Payload;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter @AllArgsConstructor
public class UpdateProfilePayload extends Payload {

    private UUID player;

}
