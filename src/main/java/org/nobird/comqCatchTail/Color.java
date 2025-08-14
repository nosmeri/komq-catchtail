package org.nobird.comqCatchTail;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class Color {
    public enum Colors {

        red("빨간색", ChatColor.RED), orange("주황색", ChatColor.GOLD), yellow("노란색", ChatColor.YELLOW), green("초록색", ChatColor.GREEN),
        aqua("하늘색", ChatColor.AQUA), blue("파랑색", ChatColor.BLUE), purple("보라색", ChatColor.DARK_PURPLE), pink("핑크색", ChatColor.LIGHT_PURPLE);

        private final String label;
        private final ChatColor color;

        Colors(String label, ChatColor color) {
            this.label = label;
            this.color = color;
        }

        public String getLabel() {
            return label;
        }

        public ChatColor getColor() {
            return color;
        }

        public static Colors fromInteger(int x) {
            switch(x) {
                case 0:
                    return red;
                case 1:
                    return orange;
                case 2:
                    return yellow;
                case 3:
                    return green;
                case 4:
                    return aqua;
                case 5:
                    return blue;
                case 6:
                    return purple;
                case 7:
                    return pink;
            }
            return null;
        }
    }

    public static Colors nextColor(Map<Colors, ArrayList<String>> players, Colors color) {
        ArrayList<Colors> colors = getLeftColor(players);
        Collections.sort(colors, new Comparator<Colors>() {
            @Override
            public int compare(Colors o1, Colors o2) {
                return o1.ordinal() - o2.ordinal();
            }
        });

        int i = colors.indexOf(color);
        if (i < colors.size() - 1) {
            return colors.get(i+1);
        } else{
            return colors.get(0);
        }
    }

    public static ArrayList<Colors> getLeftColor(Map<Colors, ArrayList<String>> players) {
        ArrayList<Colors> colors = new ArrayList<>();

        for (Map.Entry<Color.Colors, ArrayList<String>> entrySet : players.entrySet()) {
            colors.add(entrySet.getKey());
        }
        return colors;
    }
}
