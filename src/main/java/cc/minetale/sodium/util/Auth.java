package cc.minetale.sodium.util;

import cc.minetale.postman.StringUtil;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import lombok.Getter;

public class Auth {

    @Getter private static final GoogleAuthenticator googleAuth = new GoogleAuthenticator();

    public static boolean isValid(String key, int code) {
        return googleAuth.authorize(key, code);
    }

    public static String getKey() {
        return StringUtil.randomString(16);
    }

}
