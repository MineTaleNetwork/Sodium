package cc.minetale.sodium.payloads;

import cc.minetale.postman.payload.Payload;
import cc.minetale.sodium.profile.Profile;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter @AllArgsConstructor
public class FriendPayload extends Payload {

    private final Profile playerProfile;
    private final UUID targetUuid;
    private final Action action;

    public enum Action {
        REQUEST_CREATE, REQUEST_ACCEPT, REQUEST_DENY, FRIEND_REMOVE
    }

}
