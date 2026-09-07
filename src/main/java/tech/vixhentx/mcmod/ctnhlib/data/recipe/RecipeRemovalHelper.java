package tech.vixhentx.mcmod.ctnhlib.data.recipe;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Shared datapack recipe-removal registry.
 *
 * <p>
 * Filters registered here are applied before {@code RecipeManager} parses
 * incoming datapack recipes. Dynamic recipes are not affected.
 * </p>
 */
public final class RecipeRemovalHelper {

    private static final List<RemoveFilter> FILTERS = new ArrayList<>();

    private RecipeRemovalHelper() {}

    public static List<RemoveFilter> getFilters() {
        return FILTERS;
    }

    /**
     * Registers an ID-only filter. Top-level fields are combined with AND.
     */
    public static void remove(RemoveFilter filter) {
        if (filter != null) {
            FILTERS.add(filter);
        }
    }

    /**
     * Clears all registered filters before a module registers its reload rules.
     */
    public static void clear() {
        FILTERS.clear();
    }

    /**
     * ID-only recipe selector. Fields on this filter are combined with AND;
     * not excludes matching child filters and or requires a matching child filter.
     */
    public static class RemoveFilter {

        private List<String> id;
        private String idRegex;
        private String mod;
        private String type;
        private List<RemoveFilter> not;
        private List<RemoveFilter> or;

        public RemoveFilter id(String id) {
            this.id = id == null ? null : List.of(id);
            return this;
        }

        public RemoveFilter id(List<String> ids) {
            this.id = ids == null ? null : List.copyOf(ids);
            return this;
        }

        public RemoveFilter idRegex(String idRegex) {
            this.idRegex = idRegex;
            return this;
        }

        public RemoveFilter mod(String mod) {
            this.mod = mod;
            return this;
        }

        public RemoveFilter type(String type) {
            this.type = type;
            return this;
        }

        public RemoveFilter not(RemoveFilter not) {
            if (not != null) {
                if (this.not == null) {
                    this.not = new ArrayList<>();
                }
                this.not.add(not);
            }
            return this;
        }

        public RemoveFilter or(RemoveFilter or) {
            if (or != null) {
                if (this.or == null) {
                    this.or = new ArrayList<>();
                }
                this.or.add(or);
            }
            return this;
        }

        public boolean matches(ResourceLocation recipeId) {
            String id = recipeId.toString();
            if (this.id != null && !this.id.contains(id)) return false;
            if (idRegex != null && !Pattern.matches(idRegex, id)) return false;
            if (mod != null && !mod.equals(recipeId.getNamespace())) return false;
            if (type != null && !type.equals(derivedType(recipeId))) return false;

            if (not != null) {
                for (RemoveFilter filter : not) {
                    if (filter.matches(recipeId)) return false;
                }
            }

            if (or != null) {
                for (RemoveFilter filter : or) {
                    if (filter.matches(recipeId)) return true;
                }
                return false;
            }

            return true;
        }

        private static String derivedType(ResourceLocation recipeId) {
            String path = recipeId.getPath();
            int separator = path.indexOf('/');
            String firstPathSegment = separator < 0 ? path : path.substring(0, separator);
            return recipeId.getNamespace() + ":" + firstPathSegment;
        }

        @Override
        public String toString() {
            StringBuilder summary = new StringBuilder("filter[");
            boolean first = true;
            if (id != null) {
                summary.append("id=").append(id.size() == 1 ? id.get(0) : id);
                first = false;
            }
            if (idRegex != null) {
                if (!first) summary.append(", ");
                summary.append("idRegex=").append(idRegex);
                first = false;
            }
            if (mod != null) {
                if (!first) summary.append(", ");
                summary.append("mod=").append(mod);
                first = false;
            }
            if (type != null) {
                if (!first) summary.append(", ");
                summary.append("type=").append(type);
                first = false;
            }
            if (not != null) {
                for (RemoveFilter filter : not) {
                    if (!first) summary.append(", ");
                    summary.append("not=").append(filter);
                    first = false;
                }
            }
            if (or != null) {
                for (RemoveFilter filter : or) {
                    if (!first) summary.append(", ");
                    summary.append("or=").append(filter);
                    first = false;
                }
            }
            return summary.append(']').toString();
        }
    }
}
