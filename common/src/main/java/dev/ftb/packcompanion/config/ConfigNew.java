//package dev.ftb.packcompanion.config;
//
//import net.minecraft.resources.ResourceLocation;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.function.Supplier;
//
//public class ConfigNew {
//
//    static final class ConfigTestLover {
//        public static void main(String[] args) {
//            ConfigWrapper configCreator = ConfigWrapper.create(new ResourceLocation("modid", "sexyface"));
//
//            ConfigWrapper.ConfigBuilder builder = new ConfigWrapper.ConfigBuilder(configCreator)
//                    .genericValue("im-an-generic", () -> new ImSexy("Lmao"))
//                    .string("im-an-string", "dearGodWhy")
//                    .bool("im-an-bool", true)
//                    .integer("im-an-int", 12);
//
//            ConfigValue<String> imAString = builder.
//
//            System.out.println(builder);
//        }
//
//        record ImSexy(
//                String hello
//        ) {}
//    }
//
//    public static class ConfigWrapper {
//        private ResourceLocation identifier;
//        private final Map<String, ConfigValue<?>> configData = new HashMap<>();
//
//        public static ConfigWrapper create(ResourceLocation identifier) {
//            return new ConfigWrapper(identifier);
//        }
//
//        private ConfigWrapper(ResourceLocation identifier) {
//            this.identifier = identifier;
//        }
//
//        @Nullable
//        public <T> ConfigValue<T> get(String key) {
//            if (!this.configData.containsKey(key)) {
//                return null;
//            }
//
//            return (ConfigValue<T>) this.configData.get(key);
//        }
//
//        public void load() {
//            // Load from some file source
//            // Insert keys based on those values
//            // Cries self to sleep
//        }
//
//        public void save() {
//
//        }
//
//        private <T> void add(String key, Supplier<T> defaultValue) {
//            this.configData.put(key, new ConfigValue<>(
//                    this, key, defaultValue
//            ));
//        }
//
//        private static class ConfigBuilder {
//            private ConfigWrapper wrapper;
//
//            public ConfigBuilder(ConfigWrapper wrapper) {
//                this.wrapper = wrapper;
//            }
//
//            public ConfigBuilder string(String key, String defaultValue) {
//                this.wrapper.add(key, () -> defaultValue);
//                return this;
//            }
//
//            public ConfigBuilder bool(String key, boolean defaultValue) {
//                this.wrapper.add(key, () -> defaultValue);
//                return this;
//            }
//
//            public ConfigBuilder integer(String key, int defaultValue) {
//                this.wrapper.add(key, () -> defaultValue);
//                return this;
//            }
//
//            public ConfigBuilder float
//
//            public <T> ConfigBuilder genericValue(String key, Supplier<T> defaultValue) {
//                this.wrapper.add(key, defaultValue);
//                return this;
//            }
//        }
//    }
//
//    public static class ConfigValue<T> {
//        ConfigWrapper configWrapper;
//        String key;
//
//        boolean loadedCache = false;
//        Supplier<T> defaultValue;
//        T value;
//
//        public ConfigValue(ConfigWrapper configWrapper, String key, Supplier<T> defaultValue) {
//            this.configWrapper = configWrapper;
//            this.key = key;
//            this.defaultValue = defaultValue;
//        }
//
//        public T get() {
//            if (this.loadedCache) {
//                return this.value;
//            }
//
//            this.value = this.loadOrDefault();
//            this.loadedCache = true;
//            return this.value;
//        }
//
//        private T loadOrDefault() {
//            T value = this.configWrapper.get(this.key);
//            if (value == null) {
//                return this.defaultValue.get();
//            }
//
//            // Go and find the config value I guess?
//            return value;
//        }
//
//        public void save() {
//
//        }
//    }
//}
