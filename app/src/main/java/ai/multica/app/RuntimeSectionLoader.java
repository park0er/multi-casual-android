package ai.multica.app;

import java.util.List;

final class RuntimeSectionLoader {
    interface Loader<T> {
        T load() throws Exception;
    }

    private RuntimeSectionLoader() {
    }

    static <T> T load(String label, T fallback, List<String> loadErrors, Loader<T> loader) {
        try {
            return loader.load();
        } catch (Exception error) {
            loadErrors.add(label + ": " + (error.getMessage() == null ? error.toString() : error.getMessage()));
            return fallback;
        }
    }
}
