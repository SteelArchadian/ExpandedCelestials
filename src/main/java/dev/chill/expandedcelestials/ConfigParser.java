package dev.chill.expandedcelestials;

import com.google.gson.*;
import java.lang.reflect.*;

/** Rejects unknown keys and wrong JSON types instead of silently ignoring mistakes. */
final class ConfigParser {
    static Rules parse(String text) {
        JsonElement json = JsonParser.parseString(text);
        check(json, Rules.class, "config");
        return new Gson().fromJson(json, Rules.class).validate();
    }
    private static void check(JsonElement json, Type type, String path) {
        if (json.isJsonNull()) {
            if (type == Double.class || path.endsWith(".attributes")) return;
            throw new IllegalArgumentException(path + " cannot be null");
        }
        if (type instanceof ParameterizedType generic) {
            if (generic.getRawType() == java.util.List.class) {
                if (!json.isJsonArray()) throw new IllegalArgumentException(path + " must be an array");
                for (JsonElement item : json.getAsJsonArray()) check(item, generic.getActualTypeArguments()[0], path + "[]");
                return;
            }
            if (generic.getRawType() == java.util.Map.class) {
                if (!json.isJsonObject()) throw new IllegalArgumentException(path + " must be an object");
                json.getAsJsonObject().entrySet().forEach(e -> check(e.getValue(), generic.getActualTypeArguments()[1], path + "." + e.getKey()));
                return;
            }
        }
        if (type == boolean.class || type == String.class || type == double.class || type == Double.class || type == int.class) {
            boolean valid = json.isJsonPrimitive() && (type == boolean.class ? json.getAsJsonPrimitive().isBoolean()
                : type == String.class ? json.getAsJsonPrimitive().isString() : json.getAsJsonPrimitive().isNumber());
            if (!valid) throw new IllegalArgumentException("Wrong JSON type at " + path);
            if (type == int.class && json.getAsBigDecimal().stripTrailingZeros().scale() > 0) throw new IllegalArgumentException(path + " must be an integer");
            return;
        }
        if (!json.isJsonObject()) throw new IllegalArgumentException(path + " must be an object");
        Class<?> clazz = (Class<?>)type;
        for (var entry : json.getAsJsonObject().entrySet()) {
            try { check(entry.getValue(), clazz.getField(entry.getKey()).getGenericType(), path + "." + entry.getKey()); }
            catch (NoSuchFieldException e) { throw new IllegalArgumentException("Unknown setting " + path + "." + entry.getKey() + "; native EC2 drops and spawn additions use event datapacks"); }
        }
    }
}
