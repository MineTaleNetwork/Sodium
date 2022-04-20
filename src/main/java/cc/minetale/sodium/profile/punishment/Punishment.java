package cc.minetale.sodium.profile.punishment;

import cc.minetale.sodium.data.ProfileObject;
import cc.minetale.sodium.util.Colors;
import cc.minetale.sodium.util.JsonUtil;
import cc.minetale.sodium.util.Message;
import cc.minetale.sodium.util.TimeUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter @Setter
public class Punishment extends ProfileObject {

    @Getter @Setter private static MongoCollection<Document> collection;

    private PunishmentType type;

    public Punishment(
            @NotNull String uuid,
            UUID playerId,
            UUID addedById,
            long addedAt,
            String addedReason,
            long duration,
            PunishmentType type
    ) {
        super(uuid, playerId, addedById, addedAt, addedReason, duration);

        this.type = type;
    }

    public static Punishment getPunishment(String uuid) {
        var document = collection.find(Filters.eq("_id", uuid)).first();

        if (document != null) {
            return JsonUtil.readFromJson(document, Punishment.class);
        }

        return null;
    }

    public static List<Punishment> getPunishments(UUID uuid) {
        var punishments = new ArrayList<Punishment>();

        for (var document : collection.find(Filters.eq("playerId", uuid.toString()))) {
            var punishment = JsonUtil.readFromJson(document, Punishment.class);

            if (punishment != null) {
                punishments.add(punishment);
            }
        }

        return punishments;
    }

    public String getContext() {
        if (isPermanent()) {
            return (isRemoved() ? type.getUndoContext() : "permanently " + type.getContext());
        } else {
            return (isRemoved() ? type.getUndoContext() : "temporarily " + type.getContext());
        }
    }

    private String capitalize(String str) {
        if(str == null || str.isEmpty()) {
            return str;
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public List<Component> getPunishmentMessage() {
        var context = capitalize(getType().getContext());

        var message = new ArrayList<>(Arrays.asList(
                Message.chatSeparator(),
                Component.text().append(
                        Component.text("(!) ", Colors.RED, TextDecoration.BOLD),
                        Component.text((isPermanent() ? "Permanently" : "Temporarily" ) + " " + context + " ", Colors.RED),
                        Component.text("(!)", Colors.RED, TextDecoration.BOLD)
                ).build(),
                Component.empty(),
                Component.text().append(
                        Component.text(context + " on", NamedTextColor.GRAY),
                        Component.text(" » ", NamedTextColor.DARK_GRAY, TextDecoration.BOLD),
                        Component.text(TimeUtil.dateToString(new Date(getAddedAt()), true), Colors.BLUE)
                ).build(),
                Component.text().append(
                        Component.text("Reason", NamedTextColor.GRAY),
                        Component.text(" » ", NamedTextColor.DARK_GRAY, TextDecoration.BOLD),
                        Component.text(getAddedReason(), Colors.BLUE)
                ).build()
        ));

        if(!isPermanent()) {
            message.add(Component.text().append(
                    Component.text("Expires", NamedTextColor.GRAY),
                    Component.text(" » ", NamedTextColor.DARK_GRAY, TextDecoration.BOLD),
                    Component.text(getTimeRemaining(), Colors.BLUE)
            ).build());
        }

        message.addAll(Arrays.asList(
                Component.text().append(
                        Component.text("Punishment ID", NamedTextColor.GRAY),
                        Component.text(" » ", NamedTextColor.DARK_GRAY, TextDecoration.BOLD),
                        Component.text(getUuid(), Colors.BLUE)
                ).build(),
                Component.empty(),
                Component.text().append(
                        Component.text("Appeal at", NamedTextColor.GRAY),
                        Component.text(" » ", NamedTextColor.DARK_GRAY, TextDecoration.BOLD),
                        Component.text("minetale.cc/discord", Colors.BLUE, TextDecoration.UNDERLINED)
                ).build(),
                Message.chatSeparator()
        ));

        return message;
    }

}
