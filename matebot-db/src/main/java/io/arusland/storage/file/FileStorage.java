package io.arusland.storage.file;

import io.arusland.storage.Storage;
import io.arusland.storage.User;
import io.arusland.storage.UserStorage;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.util.Map;

/**
 * Created by ruslan on 03.12.2016.
 */
public class FileStorage implements Storage {
    private final static String USER_PREFIX = "user";
    private final File storageRoot;
    private final Map<String, String> params;

    public FileStorage(String storageRoot, Map<String, String> params) {
        this.storageRoot = initStorageRoot(storageRoot);
        this.params = Validate.notNull(params, "params");
    }

    public UserStorage getOrCreate(User user) {
        return new FileUserStorage(user, new File(storageRoot, USER_PREFIX + user.getId()));
    }

    private File initStorageRoot(String storageRoot) {
        File root = new File(storageRoot, "data");

        if (!root.exists()) {
            if (!root.mkdirs()) {
                throw new RuntimeException("Cannot create directory: " + root);
            }
        }

        return root;
    }
}
