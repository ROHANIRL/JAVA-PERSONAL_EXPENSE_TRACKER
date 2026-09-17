package util;

/** Minimal hand-rolled JSON string escaping - avoids adding an external library for a small REST API. */
public final class Json {
    private Json() {
    }

    public static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Wraps a value in quotes and escapes it - use for any String going into a JSON response. */
    public static String str(String value) {
        return "\"" + escape(value) + "\"";
    }
}
