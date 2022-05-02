package cc.minetale.sodium.util;

import cc.minetale.postman.StringUtil;
import cc.minetale.sodium.Sodium;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

@UtilityClass
public class Message {

    public static TextComponent CONSOLE = Component.text("Console", NamedTextColor.DARK_RED);

    public static Style style(NamedTextColor color, TextDecoration... decorations) {
        return Style.style(color, decorations).decoration(TextDecoration.ITALIC, false);
    }

    public static Component notification(String prefix, Component component) {
        return Component.text()
                .append(
                        Component.text(prefix, Colors.BLUE, TextDecoration.BOLD),
                        Component.text(" » ", NamedTextColor.DARK_GRAY, TextDecoration.BOLD),
                        component
                ).build();
    }

    public static Component separator(int times) {
        return Component.text(StringUtil.repeat(" ", times), NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH);
    }

    public static Component chatSeparator() {
        return separator(80);
    }

    public static Component menuSeparator() {
        return separator(50);
    }

    public static Component scoreboardSeparator() {
        return separator(32);
    }

    public static Component gradientComponent(String text, TextColor from, TextColor to) {
        var builder = Component.text();

        int steps = text.length();
        int step = 0;

        for (char c : text.toCharArray()) {
            builder.append(Component.text(c, Colors.getColorBetween(from, to, step / (float) steps)));

            step++;
        }

        return builder.build();
    }

    public static Component parse(String input, Object... replacements) {
        var mini = Sodium.getMiniMessage();

        for (int i = 0; i < replacements.length; i++) {
            var key = "<" + i + ">";
            var replacement = replacements[i];

            if (replacement instanceof Component component) {
                input = input.replace(key, mini.serialize(component));
            } else {
                input = input.replace(key, replacement.toString());
            }
        }

        return mini.deserialize(input).applyFallbackStyle(Style.style(NamedTextColor.WHITE, TextDecoration.ITALIC.withState(false)));
    }

}
