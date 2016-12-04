package io.arusland.storage;

/**
 * Heterogeneous storage.
 * <p>
 * Created by ruslan on 03.12.2016.
 */
public interface Storage {
    UserStorage getOrCreate(User user);
}
