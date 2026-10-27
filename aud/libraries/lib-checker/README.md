# Lib Checker

This library contains a framework for checking the validity of AUDs.

## Check Types

Whenever you want to create a new check, you must decide what kind of check it is by implementing one of the `CheckType`
interfaces.

## Usage

See how it's used in `checker`.

## ⚠️ WARNING

No checks should be implemented in this library. Implement them in another lib or in the `checker` module instead.
