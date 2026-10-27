package com.amadeus.aud.libraries.checker.types;

import com.amadeus.aud.libraries.checker.models.Message;
import java.nio.file.Path;
import java.util.Collection;

/** Checks that operate on only knowing a file path, aimed to validate files themselves. */
@FunctionalInterface
public non-sealed interface FileCheck extends CheckType {
  Collection<Message> check(CharSequence domain, Path path);
}
