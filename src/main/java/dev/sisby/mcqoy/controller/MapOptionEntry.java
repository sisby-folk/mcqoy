package dev.sisby.mcqoy.controller;

import com.google.common.collect.ImmutableSet;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionFlag;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface MapOptionEntry<T> extends Option<T> {
    MapOption<T> parentGroup();

    @Override
    default @NotNull ImmutableSet<OptionFlag> flags() {
        return parentGroup().flags();
    }

    @Override
    default boolean available() {
        return parentGroup().available();
    }

    @NotNull Map.Entry<String, T> pendingEntry();
}
