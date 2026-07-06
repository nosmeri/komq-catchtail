package org.nobird.komqCatchTail;

import org.bukkit.ChatColor;

import java.util.*;

public class Color {

    public enum Colors {

        RED("빨간색", ChatColor.RED),
        ORANGE("주황색", ChatColor.GOLD),
        YELLOW("노란색", ChatColor.YELLOW),
        GREEN("초록색", ChatColor.GREEN),
        AQUA("하늘색", ChatColor.AQUA),
        BLUE("파랑색", ChatColor.BLUE),
        PURPLE("보라색", ChatColor.DARK_PURPLE),
        PINK("핑크색", ChatColor.LIGHT_PURPLE);

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

        /**
         * 정수 인덱스로 Colors 열거형 값을 반환합니다.
         * 범위를 벗어나면 null을 반환합니다.
         */
        public static Colors fromInteger(int x) {
            Colors[] values = Colors.values();
            if (x < 0 || x >= values.length) return null;
            return values[x];
        }
    }

    /**
     * 현재 팀의 다음 타겟 색상을 반환합니다.
     * 남은 팀 목록을 ordinal 순으로 정렬 후 순환합니다.
     */
    public static Colors nextColor(Map<Colors, ArrayList<String>> players, Colors color) {
        ArrayList<Colors> colors = getLeftColors(players);
        colors.sort(Comparator.comparingInt(Enum::ordinal));

        int i = colors.indexOf(color);
        if (i < 0) return colors.get(0); // color가 목록에 없을 경우 첫 번째 반환
        return colors.get((i + 1) % colors.size());
    }

    /**
     * 현재 게임에 존재하는 팀 색상 목록을 반환합니다.
     */
    public static ArrayList<Colors> getLeftColors(Map<Colors, ArrayList<String>> players) {
        return new ArrayList<>(players.keySet());
    }
}
