package ch.sbb.polarion.extension.api_extender.velocity.livereport;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads and writes a Polarion Velocity v1.4 context by reflection.
 * <p>
 * This code compiles against Velocity 2, where {@code getKeys()} returns {@code String[]}.
 * Velocity v1.4 returns {@code Object[]}, so a direct call fails with {@link NoSuchMethodError}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Velocity14Context {

    @SneakyThrows
    public static @NotNull Map<String, Object> toMap(@NotNull Object velocityContext) {
        Object[] keys = (Object[]) velocityContext.getClass().getMethod("getKeys").invoke(velocityContext);
        Method getter = velocityContext.getClass().getMethod("get", String.class);
        Map<String, Object> variables = new HashMap<>();
        for (Object key : keys) {
            String name = String.valueOf(key);
            variables.put(name, getter.invoke(velocityContext, name));
        }
        return variables;
    }

    @SneakyThrows
    public static void put(@NotNull Object velocityContext, @NotNull String key, @Nullable Object value) {
        velocityContext.getClass().getMethod("put", String.class, Object.class).invoke(velocityContext, key, value);
    }
}
