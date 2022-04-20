package cc.minetale.sodium.profile.grant;

import cc.minetale.sodium.profile.Profile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter @AllArgsConstructor
public enum Rank {
    OWNER("Owner", NamedTextColor.DARK_RED, true, false),
    ADMIN("Admin", NamedTextColor.RED, true, false),
    MOD("Mod", NamedTextColor.DARK_PURPLE, true, false),
    HELPER("Helper", NamedTextColor.BLUE, true, false),

    YOUTUBE("YouTube", NamedTextColor.RED, false, false),
    MEDIA("Media", NamedTextColor.LIGHT_PURPLE, false, false),

    HIGHROLLER("Highroller", NamedTextColor.DARK_PURPLE, false, true),
    LEGEND("Legend", NamedTextColor.GOLD, false, true),
    MVP("MVP", NamedTextColor.DARK_AQUA, false, true),
    VIP("VIP", NamedTextColor.DARK_GREEN, false, true),
    PREMIUM("Premium", NamedTextColor.GREEN, false, true),

    MEMBER("Member", NamedTextColor.GRAY, false, false);

    private final String name;
    private final NamedTextColor color;
    private final boolean staff;
    private final boolean sync;

    public Component getPrefix() {
        return Component.text().append(
                Component.text("[", NamedTextColor.DARK_GRAY),
                Component.text(name, color),
                Component.text("]", NamedTextColor.DARK_GRAY)
        ).build();
    }

    public static boolean hasMinimumRank(Profile profile, Rank rank) {
        return profile.getGrant().getRank().compare(rank);
    }

    public boolean compare(Rank other) {
        return this.ordinal() <= other.ordinal();
    }

}
