package cc.minetale.sodium.payloads;

import cc.minetale.postman.payload.Payload;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter @AllArgsConstructor
public class ConversationPayload extends Payload {

    private final UUID player;
    private final UUID target;
    private final String message;

}
