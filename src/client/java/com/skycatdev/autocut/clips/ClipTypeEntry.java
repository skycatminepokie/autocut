package com.skycatdev.autocut.clips;

import com.mojang.serialization.Codec;

public record ClipTypeEntry<T extends ClipType>(Codec<T> codec, T clipType) {
}
