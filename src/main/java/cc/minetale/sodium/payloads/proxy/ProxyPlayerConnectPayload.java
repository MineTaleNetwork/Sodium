package cc.minetale.sodium.payloads.proxy;

import cc.minetale.postman.payload.Payload;
import cc.minetale.sodium.profile.Profile;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class ProxyPlayerConnectPayload extends Payload {

    private Profile profile;
    private String server;

}
