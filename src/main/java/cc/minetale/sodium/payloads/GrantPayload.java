package cc.minetale.sodium.payloads;

import cc.minetale.postman.payload.Payload;
import cc.minetale.sodium.profile.grant.Grant;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter @AllArgsConstructor
public class GrantPayload extends Payload {

    private UUID player;
    private Action action;
    private Grant grant;

    public enum Action {
        ADD,
        REMOVE,
        EXPIRE
    }

}