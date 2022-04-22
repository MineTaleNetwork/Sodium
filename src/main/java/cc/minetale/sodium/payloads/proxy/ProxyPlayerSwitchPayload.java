package cc.minetale.sodium.payloads.proxy;

import cc.minetale.postman.payload.Payload;
import cc.minetale.sodium.profile.Profile;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class ProxyPlayerSwitchPayload extends Payload {

    private Profile profile;
    private String serverTo;
    private String serverFrom;

}
