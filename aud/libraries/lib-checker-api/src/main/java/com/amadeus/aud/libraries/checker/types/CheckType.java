package com.amadeus.aud.libraries.checker.types;

/**
 * Marker interface for all check types.
 *
 * <p>This interface is used to categorize checks that can be performed by the checker.
 */
public sealed interface CheckType
    permits FileCheck, DomainCheck, ModelsCheck, BomCheck, OtherCheck, GlobalCheck {}
