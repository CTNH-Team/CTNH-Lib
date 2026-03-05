package tech.vixhentx.mcmod.ctnhlib.data;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static tech.vixhentx.mcmod.ctnhlib.CTNHLib.MODID;

public class DataFilterPack implements PackResources {

    final String name;
    public static final Set<RawRL> FILTERED = new HashSet<>();

    public DataFilterPack(String name) {
        this.name = name;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... strings) {
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType packType, ResourceLocation resourceLocation) {
        return null;
    }

    @Override
    public void listResources(PackType packType, String s, String s1, ResourceOutput resourceOutput) {}

    public static void removeRecipe(String s) {
        FILTERED.add(RawRL.of(s));
    }

    public static void removeRecipe(String nameSpace, String path) {
        FILTERED.add(RawRL.of(nameSpace, path));
    }

    public static void removeRecipeType(String s) {
        FILTERED.add(RawRL.ofType(s));
    }

    public static void removeRecipeType(String nameSpace, String path) {
        FILTERED.add(RawRL.ofType(nameSpace, path));
    }

    public static void removeData(String nameSpace, String path) {
        FILTERED.add(RawRL.ofRaw(nameSpace, path));
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        return Set.of(MODID + "_filter");
    }

    @Override
    public @Nullable <T> T getMetadataSection(@NotNull MetadataSectionSerializer<T> metaReader) throws IOException {
        if (metaReader == PackMetadataSection.TYPE) {
            return (T) new PackMetadataSection(Component.literal("CTNH Filter Data"), 15);

        } else if (metaReader.getMetadataSectionName().equals("filter")) {
            JsonObject filter = new JsonObject();
            JsonArray block = new JsonArray();

            for (var rl : FILTERED) {
                JsonObject entry = new JsonObject();
                entry.addProperty("namespace", rl.nameSpace);
                entry.addProperty("path", rl.path);
                block.add(entry);
            }

            filter.add("block", block);
            return metaReader.fromJson(filter);
        }
        return null;
    }

    @Override
    public String packId() {
        return name;
    }

    @Override
    public void close() {}

    @Override
    public boolean isBuiltin() {
        return true;
    }

    public record RawRL(String nameSpace, String path) {

        private static final String NAMESPACE_REGEX = "^\\w+$";
        private static final String PATH_REGEX = "^[\\w/]+$";

        /* ==================== public factory methods ==================== */

        public static RawRL of(String s) {
            var sp = splitAndValidate(s);
            return new RawRL(
                    validateNamespace(sp[0]),
                    "^recipes/" + validatePath(sp[1]) + ".json$");
        }

        public static RawRL of(String nameSpace, String path) {
            return new RawRL(
                    validateNamespace(nameSpace),
                    "^recipes/" + validatePath(path) + ".json$");
        }

        public static RawRL ofType(String s) {
            var sp = splitAndValidate(s);
            return new RawRL(
                    validateNamespace(sp[0]),
                    "^recipes/" + sp[1]);
        }

        public static RawRL ofType(String nameSpace, String path) {
            return new RawRL(
                    validateNamespace(nameSpace),
                    "^recipes/" + path);
        }

        public static RawRL ofRaw(String nameSpace, String path) {
            return new RawRL(validateNamespace(nameSpace), path);
        }
        /* ==================== validation helpers ==================== */

        private static String[] splitAndValidate(String s) {
            if (s == null) {
                throw new IllegalArgumentException("Input string must not be null");
            }

            int first = s.indexOf(':');
            int last = s.lastIndexOf(':');

            if (first < 0) {
                throw new IllegalArgumentException(
                        "Invalid resource location '" + s + "': missing ':'");
            }
            if (first != last) {
                throw new IllegalArgumentException(
                        "Invalid resource location '" + s + "': more than one ':'");
            }

            String nameSpace = s.substring(0, first);
            String path = s.substring(first + 1);

            if (nameSpace.isEmpty() || path.isEmpty()) {
                throw new IllegalArgumentException(
                        "Invalid resource location '" + s + "': namespace or path is empty");
            }

            return new String[] { nameSpace, path };
        }

        private static String validateNamespace(String nameSpace) {
            if (!nameSpace.matches(NAMESPACE_REGEX)) {
                throw new IllegalArgumentException(
                        "Invalid namespace '" + nameSpace + "': only \\w characters are allowed");
            }
            return nameSpace;
        }

        private static String validatePath(String path) {
            if (!path.matches(PATH_REGEX)) {
                throw new IllegalArgumentException(
                        "Invalid path '" + path + "': only \\w and '/' characters are allowed");
            }
            return path;
        }
    }
}
