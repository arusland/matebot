package io.arusland.storage;

import io.arusland.storage.file.FileStorage;

import java.util.Map;

/**
 * Created by ruslan on 03.12.2016.
 */
public class StorageFactory {
    public static Storage createStorage(String storageRoot, Map<String, String> params) {
        return new FileStorage(storageRoot, params);
    }
}
