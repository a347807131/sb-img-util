/*
 * Copyright (c) 2020 Weasis Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package fun.gatsby.sbimgutil.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(StringUtil.class);

    public static final String EMPTY_STRING = "";
    public static final String SPACE = " ";
    public static final String COLON = ":";
    public static final String COLON_AND_SPACE = ": ";

    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final int[] EMPTY_INT_ARRAY = new int[0];

    private static final char[] HEX_DIGIT = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    public enum Suffix {
        NO(""),

        UNDERSCORE("_"),

        ONE_PTS("."),

        THREE_PTS("...");

        private final String value;

        Suffix(String suffix) {
            this.value = suffix;
        }

        public String getValue() {
            return value;
        }

        public int getLength() {
            return value.length();
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private StringUtil() {}

    public static String getTruncatedString(String name, int limit, Suffix suffix) {
        if (name != null && name.length() > limit) {
            int sLength = suffix.getLength();
            int end = limit - sLength;
            if (end > 0 && end + sLength < name.length()) {
                return name.substring(0, end).concat(suffix.getValue());
            }
        }
        return name;
    }

    public static Character getFirstCharacter(String val) {
        if (StringUtil.hasText(val)) {
            return val.charAt(0);
        }
        return null;
    }

    public static String[] getStringArray(String val, String delimiter) {
        if (delimiter != null && StringUtil.hasText(val)) {
            return val.split(Pattern.quote(delimiter));
        }
        return EMPTY_STRING_ARRAY;
    }

    public static int[] getIntegerArray(String val, String delimiter) {
        if (delimiter != null && StringUtil.hasText(val)) {
            String[] vl = val.split(Pattern.quote(delimiter));
            int[] res = new int[vl.length];
            for (int i = 0; i < res.length; i++) {
                res[i] = getInt(vl[i]);
            }
            return res;
        }
        return EMPTY_INT_ARRAY;
    }

    public static Integer getInteger(String val) {
        if (StringUtil.hasText(val)) {
            try {
                return Integer.parseInt(val.trim());
            } catch (NumberFormatException e) {
                LOGGER.warn("Cannot parse {} to Integer", val);
            }
        }
        return null;
    }

    public static int getInt(String val) {
        if (StringUtil.hasText(val)) {
            try {
                return Integer.parseInt(val.trim());
            } catch (NumberFormatException e) {
                LOGGER.warn("Cannot parse {} to int", val);
            }
        }
        return 0;
    }

    public static int getInt(String value, int defaultValue) {
        if (value != null) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                LOGGER.warn("Cannot parse {} to int", value);
            }
        }
        return defaultValue;
    }

    public static Double getDouble(String val) {
        if (StringUtil.hasText(val)) {
            try {
                return Double.parseDouble(val.trim());
            } catch (NumberFormatException e) {
                LOGGER.warn("Cannot parse {} to Double", val);
            }
        }
        return null;
    }

    public static String splitCamelCaseString(String s) {
        StringBuilder builder = new StringBuilder();
        for (String w : s.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
            builder.append(w);
            builder.append(' ');
        }
        return builder.toString().trim();
    }

    public static boolean hasLength(CharSequence str) {
        return str != null && str.length() > 0;
    }

    public static boolean hasLength(String str) {
        return hasLength((CharSequence) str);
    }

    public static boolean hasText(CharSequence str) {
        if (!hasLength(str)) {
            return false;
        }
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasText(String str) {
        return hasText((CharSequence) str);
    }

    /**
     * Removing diacritical marks aka accents
     *
     * @param str
     * @return the input string without accents
     */
    public static String deAccent(String str) {
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

    /**
     * @param s
     * @return the list of words or part with quotes
     */
    public static List<String> splitSpaceExceptInQuotes(String s) {
        if (s == null) {
            return Collections.emptyList();
        }
        List<String> matchList = new ArrayList<>();
        Pattern patternSpaceExceptQuotes = Pattern.compile("'[^']*'|\"[^\"]*\"|( )");
        Matcher m = patternSpaceExceptQuotes.matcher(s);
        StringBuffer b = new StringBuffer();
        while (m.find()) {
            if (m.group(1) == null) {
                m.appendReplacement(b, m.group(0));
                String arg = b.toString();
                b.setLength(0);
                if (StringUtil.hasText(arg)) {
                    matchList.add(arg);
                }
            }
        }
        b.setLength(0);
        m.appendTail(b);
        String arg = b.toString();
        if (StringUtil.hasText(arg)) {
            matchList.add(arg);
        }
        return matchList;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_DIGIT[v >>> 4];
            hexChars[j * 2 + 1] = HEX_DIGIT[v & 0x0f];
        }
        return new String(hexChars);
    }

    public static String integerToHex(int val) {
        return Integer.toHexString(val).toUpperCase();
    }

    public static String bytesToMD5(byte[] val) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5"); // NOSONAR not a security issue
        return bytesToHex(md.digest(val));
    }

    public static String getNullIfNull(Object object) {
        if (object == null) {
            return null;
        }
        return object.toString();
    }

    public static String getEmptyStringIfNull(Object object) {
        if (object == null) {
            return EMPTY_STRING;
        }
        return object.toString();
    }

    public static String getEmptyStringIfNullEnum(Enum<?> object) {
        if (object == null) {
            return EMPTY_STRING;
        }
        return object.name();
    }
}
