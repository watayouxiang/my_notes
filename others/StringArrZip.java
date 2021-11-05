package watayouxiang.file.question;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * 字符串数组中的字符串的压缩算法实现：
 * {internet, good, god, aabbcc, abbbcc}
 * 头尾保持不变，中间显示字符数。
 * 若压缩后出现重复的字符串则进行保留多一位，直至不同或者完全显示。
 *
 * <p>
 * internet --> i6t
 * good --> g2d
 * god --> god
 * aabbcc --> a4c --> aa3c
 * abbbcc --> a4c --> ab3c
 * <p>
 * <p>
 * {i6t, g2d, god, aa3c, ab3c}
 */
public class StringArrZip {
    public static void main(String[] args) {
        String[] stringArr = {
                "me", null, "", "a", "god", "internet",
                "good", "good",
                "aabbcc", "abbbcc"
        };
        System.out.println(Arrays.toString(stringArr));

        String[] newStringArr = handleStringArr(stringArr);
        System.out.println(Arrays.toString(newStringArr));
    }

    private static String[] handleStringArr(String[] strings) {
        if (strings == null || strings.length == 0) return strings;

        // 处理字符串
        int length = strings.length;
        List<HandleStr> list = new ArrayList<>();
        for (String string : strings) {
            HandleStr handleStr = new HandleStr(string);
            handleStr.handle();
            list.add(handleStr);
        }

        // 返回结果字符串数组
        String[] newStrings = new String[length];
        for (int i = 0; i < length; i++) {
            newStrings[i] = list.get(i).handleString;
        }

        // 再次处理
        if (isAgain(list)) {
            handleStringArr(newStrings);
        }

        return newStrings;
    }

    private static boolean isAgain(List<HandleStr> list) {
        int size = list.size();
        HashSet<HandleStr> set = new HashSet<>(list);
        int setSize = set.size();
        if (size != setSize) {
            for (HandleStr handleStr : list) {
                return handleStr.isHandle();
            }
        }
        return false;
    }

    public static class HandleStr {
        public HandleStr(String string) {
            this.string = string;
        }

        public String string;
        public String handleString;
        public int index;
        public boolean isHandle;

        public boolean isHandle() {
            return isHandle;
        }

        public void handle() {
            if (string == null || "".equals(string)) {
                handleString = string;
                return;
            }

            int len = string.length();
            if (len <= 3) {
                handleString = string;
                return;
            }

            if (index + 1 <= string.length()) {
                index++;
            }

            String startChar = string.substring(0, index);
            String endChar = string.substring(len - index, len);
            String handleStr = string.substring(1, len - index);
            int handleStrLen = handleStr.length();

            handleString = startChar + handleStrLen + endChar;

            isHandle = !string.equals(handleString);
        }
    }
}
