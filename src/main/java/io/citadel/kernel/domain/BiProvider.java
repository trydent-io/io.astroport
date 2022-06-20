package io.citadel.kernel.domain;

import io.citadel.kernel.func.ThrowableBiFunction;

public interface BiProvider<S, T, R> extends ThrowableBiFunction<S, T, R> {
}
