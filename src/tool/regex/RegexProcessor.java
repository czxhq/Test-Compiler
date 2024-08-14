package tool.regex;

import java.util.HashMap;
import java.util.Stack;

public class RegexProcessor {
    public static String special = "[]()*+?:$^\\-.";

    /**
     * @param regex the regex that needs to be normalized
     * @return the normalized regex
     * @throws RegexException when the regex has grammar mistake
     */
    public static String process(String regex) throws RegexException {
        // process '[]'
        String temp1 = "";
        for (int i = 0; i < regex.length(); i++) {
            if (regex.charAt(i) == '\\') {
                if (i + 1 >= regex.length()) {
                    throw new RegexException();
                }
                if (special.contains(regex.substring(i + 1, i + 2))) {
                    temp1 += regex.substring(i, i + 2);
                    i++;
                    continue;
                } else {
                    throw new RegexException();
                }
            }
            if (regex.charAt(i) != '[') {
                temp1 += regex.substring(i, i + 1);
            } else {
                String temp2 = truncate(regex, i);
                temp1 += expand(temp2);
                i += temp2.length() - 1;
            }
        }
        // process '.'
        String temp2 = "";
        for (int i = 0; i < temp1.length(); i++) {
            if (temp1.charAt(i) == '\\') {
                temp2 += temp1.substring(i, i + 2);
                i++;
                continue;
            }
            if (temp1.charAt(i) == '.') {
                temp2 += getNNL();
            } else {
                temp2 += temp1.substring(i, i + 1);
            }
        }
        // process '+'
        HashMap<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < temp2.length(); i++) {
            if (temp2.charAt(i) == '\\') {
                i++;
                continue;
            }
            if (temp2.charAt(i) == '(') {
                int j = findRightBracket(temp2, i);
                if (j == -1) {
                    throw new RegexException();
                }
                map.put(j, i);
                map.put(i, j);
            }
            if (temp2.charAt(i) == ')' && !map.containsKey(i)) {
                throw new RegexException();
            }
        }
        String temp3 = processPlus(temp2, 0, map);
        return temp3;
    }

    /**
     * @return the corresponding normalized regex of .
     */
    public static String getNNL() {
        String res = "(";
        for (int i = 0; i < 256; i++) {
            if ((char) i != '\n') {
                if (special.contains(Character.toString((char) i))) {
                    res += "\\" + Character.toString((char) i);
                } else {
                    res += Character.toString((char) i);
                }
                if (i != 255)
                    res += "|";
            }
        }
        res += ")";
        return res;
    }

    /**
     * @param regex where to get the truncated regex
     * @param index where to truncate, start from the "["
     * @return truncated string
     * @throws RegexException when the truncated part has error
     */
    public static String truncate(String regex, int index) throws RegexException {
        int lastIndex = -1;
        for (int i = index + 1; i < regex.length(); i++) {
            char c = regex.charAt(i);
            if (i == index + 1) {
                // if c is special, it can only be '^' and '\', cannot be ']' because it's empty
                if (special.contains(Character.toString(c)) && c != '^' && c != '\\') {
                    throw new RegexException();
                }
            }
            if (c == '\\') {
                if (i + 1 >= regex.length()) {
                    throw new RegexException();
                }
                char c1 = regex.charAt(i + 1);
                // the char after / can only be special
                if (!special.contains(Character.toString(c1))) {
                    throw new RegexException();
                }
                i++;
            } else if (c == ']') {
                lastIndex = i + 1;
                break;
            } else if (c == '-') {
                if (i - 1 < index || i + 1 >= regex.length()) {
                    throw new RegexException();
                }
                char left = regex.charAt(i - 1);
                char right = regex.charAt(i + 1);
                // only the right direction, 0-9a-zA-Z can extend
                if (!canExtend(left, right)) {
                    throw new RegexException();
                }
            } else if (i != index + 1 && special.contains(Character.toString(c))) { // in the common area can't contain some special char
                throw new RegexException();
            }
        }
        // '[' mismatch
        if (lastIndex == -1) {
            throw new RegexException();
        }
        return regex.substring(index, lastIndex);
    }

    public static String expand(String regex) {
        char c = regex.charAt(1);
        String res = "(";
        boolean[] flag = new boolean[256];
        if (c == '^') {
            for (int i = 0; i < 256; i++) {
                flag[i] = true;
            }
            for (int i = 2; i < regex.length() - 1; i++) {
                char c1 = regex.charAt(i);
                if (c1 == '\\') {
                    i++;
                    flag[(int) regex.charAt(i)] = false;
                } else if (c1 == '-') {
                    char left = regex.charAt(i - 1);
                    char right = regex.charAt(i + 1);
                    for (int j = (int) left; j <= (int) right; j++) {
                        flag[j] = false;
                    }
                } else {
                    flag[(int) c1] = false;
                }
            }
        } else {
            for (int i = 1; i < regex.length() - 1; i++) {
                char c1 = regex.charAt(i);
                if (c1 == '\\') {
                    i++;
                    flag[(int) regex.charAt(i)] = true;
                } else if (c1 == '-') {
                    char left = regex.charAt(i - 1);
                    char right = regex.charAt(i + 1);
                    for (int j = (int) left; j <= (int) right; j++) {
                        flag[j] = true;
                    }
                } else {
                    flag[(int) c1] = true;
                }
            }
        }
        for (int i = 0; i < 256; i++) {
            if (flag[i]) {
                if (special.contains(Character.toString((char) i))) {
                    res += "\\" + Character.toString((char) i) + "|";
                } else {
                    res += Character.toString((char) i) + "|";
                }
            }
        }
        res = res.substring(0, res.length() - 1) + ")";
        return res;
    }

    public static boolean canExtend(char c1, char c2) {
        if (Character.isDigit(c1)) {
            if (Character.isDigit(c2) && c2 > c1) {
                return true;
            }
        }
        if (Character.isUpperCase(c1)) {
            if (Character.isUpperCase(c2) && c2 > c1) {
                return true;
            }
        }
        if (Character.isLowerCase(c1)) {
            if (Character.isLowerCase(c2) && c2 > c1) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param regex
     * @param index start from the '('
     * @return the index of the ')' or -1
     */
    public static int findRightBracket(String regex, int index) {
        Stack<Integer> st = new Stack<>();
        st.push(index);
        for (int i = index + 1; i < regex.length(); i++) {
            if (regex.charAt(i) == '\\') {
                i++;
                continue;
            }
            if (regex.charAt(i) == '(') {
                st.push(i);
            }
            if (regex.charAt(i) == ')') {
                st.pop();
                if (st.isEmpty()) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     *
     * @param regex the regex to be processed
     * @param left where to start processing
     * @param map the bracket index map
     * @return the regex that does not contains '+'
     * @throws RegexException
     */
    public static String processPlus
            (String regex, int left, HashMap<Integer, Integer> map) throws RegexException {
        String res = "";
        for (int i = left; i < regex.length() && regex.charAt(i) != ')'; i++) {
            if (regex.charAt(i) == '\\') {
                if (i + 2 < regex.length() && regex.charAt(i + 2) == '+') {
                    res += "(" + regex.substring(i, i + 2) + "|" + regex.substring(i, i + 2) + "*)";
                    i += 2;
                } else {
                    res += regex.substring(i, i + 2);
                    i++;
                }
                continue;
            }
            if (regex.charAt(i) == '(') {
                String tmp = processPlus(regex, i + 1, map);
                i = map.get(i);
                if (i + 1 < regex.length() && regex.charAt(i + 1) == '+') {
                    res += "((" + tmp + ")(" + tmp + ")*" + ")";
                    i++;
                } else {
                    res += "(" + tmp + ")";
                }
            } else if (i + 1 < regex.length() && regex.charAt(i + 1) == '+') {
                if (special.contains(regex.substring(i, i + 1))) {
                    if (i == left || regex.charAt(i - 1) != '\\') {
                        throw new RegexException();
                    }
                    res += "(" + regex.substring(i - 1, i + 1) + "|" +
                            regex.substring(i - 1, i + 1) + "*)";
                } else {
                    res += "(" + regex.substring(i, i + 1) + "|" + regex.substring(i, i + 1) + "*)";
                }
                i++;
            } else if (regex.charAt(i) == '+') {
                throw new RegexException();
            } else {
                res += regex.substring(i, i + 1);
            }
        }
        return res;
    }
}
