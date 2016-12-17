package io.arusland.storage.file;

import io.arusland.storage.Storage;
import io.arusland.storage.User;
import io.arusland.storage.UserStorage;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Created by ruslan on 03.12.2016.
 */
public class FileStorage implements Storage {
    private final static String USER_PREFIX = "user";
    private final File storageRoot;
    private final Map<String, String> params;

    public FileStorage(String storageRoot, Map<String, String> params) {
        this.storageRoot = initStorageRoot(Validate.notBlank(storageRoot, "storageRoot"));
        this.params = Validate.notNull(params, "params");
    }

    public UserStorage getOrCreate(User user) {
        return new FileUserStorage(user, new File(storageRoot, USER_PREFIX + user.getId()));
    }

    @Override
    public List<User> listUsers() {
        return Arrays.stream(storageRoot.listFiles())
                .filter(p -> p.isDirectory() && p.getName().startsWith(USER_PREFIX))
                .map(p -> fromUserDir(p))
                .collect(toList());
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

    private static User fromUserDir(File userDir) {
        String userId = userDir.getName()
                .substring(USER_PREFIX.length());

        // TODO: return real name of the user not user id
        return new User(Long.parseLong(userId), userId);
    }
}
