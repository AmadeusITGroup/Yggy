# ADR-010: Unit-tests formats and libraries

- Status: Accepted
- Date: 2025-12-01
- Deciders: AUD Engineering Team
- Supersedes: None

## Context

Unit tests are crucial to the quality of the software. However, they can be written in different styles and different
libraries can be used. This document is about standardizing the format and the library used to make them similar across
the codebase.

In scope:

- unit-tests only

⚠️ Out of scope:

- integration tests (aka, all tests named xxxIT.java)
- all native tests

For those, refer to [Quarkus Documentation](https://quarkus.io/guides/getting-started-testing).

## Problem / Forces

- Unify the format of unit-tests across the codebase
- Limit to a single testing library to reduce cognitive load

## Decision

Each test function should follow the [Arrange Act Assert (AAA)](https://wiki.c2.com/?ArrangeActAssert) (also known as
Given-When-Then).

The library to be used for unit-tests is [JUnit 5](https://junit.org/junit5/) for the test framework, and
[AssertJ](https://assertj.github.io/doc/) for assertions and [jqwik](https://jqwik.net/) for Property-Based Testing
(PBT), and [Mockito](https://site.mockito.org/) for mocking.

The class being tested MAY be called `sut` (for System Under Test) in the test code, but this is not mandatory.

Here is an example of a simple test:

```java
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CalculatorTest {
    @Test
    void addsTwoNumbersCorrectly() {
        // Arrange
        Calculator sut = new Calculator();

        // Act
        int result = sut.add(2, 3);

        // Assert
        assertThat(result).isEqualTo(5);
    }
}
```

Here is an example of a test testing multiple values with the same test. This is only use jupiter parameterized tests,
not jqwik, but consider using jqwik for more complex property-based tests.

```java
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


class CalculatorTest {
    @ParameterizedTest
    @ValueSource(ints = {1, 81, 42})
    void addingAPositiveNumberToZeroIsPositive(int number) {
        // Arrange
        Calculator sut = new Calculator();

        // Act
        int result = sut.add(0, number);

        // Assert
        assertThat(result).isPositive();
    }
}
```

Here is an example of a test with a mocked dependency:

```java
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class UserServiceTest {
    @Test
    void retrievesUserByIdNominal() {
        // Arrange
        UserRepository mockRepository = mock(UserRepository.class);
        doReturn(new User(1, "Alice")).when(mockRepository).findById(1);

        // sut is created directly with constructor instead of using @InjectMocks for clarity
        UserService sut = new UserService(mockRepository);

        // Act
        User user = sut.getUserById(1);

        // Assert
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("Alice");
    }
}
```

Note: if you want to group tests together, you SHOULD use nested test classes with `@Nested` annotation from JUnit 5.

This is often useful to group tests for a specific method or feature, or to group error test cases and successful test
cases together.

Eg:

```java
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class CalculatorTest {

    @Nested
    class AddTests {
        @Test
        void addsTwoNumbersCorrectly() {
            // Arrange
            Calculator sut = new Calculator();

            // Act
            int result = sut.add(2, 3);

            // Assert
            assertThat(result).isEqualTo(5);
        }
    }

    @Nested
    class SubtractTests {
        @Test
        void subtractsTwoNumbersCorrectly() {
            // Arrange
            Calculator sut = new Calculator();

            // Act
            int result = sut.subtract(5, 3);

            // Assert
            assertThat(result).isEqualTo(2);
        }
    }
}
```

Here is an example using jqwik for property-based testing:

```java
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import static org.assertj.core.api.Assertions.assertThat;

class CalculatorPropertyTest {
    @Property
    void additionIsCommutative(@ForAll @IntRange(min = -1000, max = 1000) int a,
                              @ForAll @IntRange(min = -1000, max = 1000) int b) {
        // Arrange
        Calculator sut = new Calculator();

        // Act
        int result1 = sut.add(a, b);
        int result2 = sut.add(b, a);

        // Assert
        assertThat(result1).isEqualTo(result2);
    }
    @Property
    void addingZeroDoesNotChangeValue(@ForAll @IntRange(min = -1000, max = 1000) int number) {
        // Arrange
        Calculator sut = new Calculator();

        // Act
        int result = sut.add(number, 0);

        // Assert
        assertThat(result).isEqualTo(number);
    }
}
```

### Naming Conventions

- Test class names MUST end with `Test` (e.g., `CalculatorTest`)
- Property-based test class names MAY end with `PropertyTest` (e.g., `CalculatorPropertyTest`)
- Test method names SHOULD be descriptive and use camelCase.
- Test method names SHOULD describe what is being tested and the expected outcome.
- @DisplayName annotation from JUnit 5 MAY be used for more descriptive names, especially for complex scenarios.
- The class being tested MAY be called `sut` (for System Under Test) in the test code.

### Best Practices

- **Test organization**: Use nested test classes with `@Nested` annotation to group related tests (e.g., by method or
  feature, or success/error cases)
- **Test independence**: Each test should be independent and not rely on the state from other tests.
- **Use parameterized tests for simple value testing**: When testing the same logic with different input values
- **Use jqwik for complex property testing**: When you need to test mathematical properties or complex invariants
- **Mock only direct dependencies**: Don't mock value objects or simple data structures. Aka, only mock classes used in
  the class constructor.
- **Prefer constructor injection over field injection**: For better testability and clarity (Aka, avoid using
  `@InjectMocks` from Mockito)
- **Mocks** are expensive: do not use them for value classes.
- **Mockito.lenient** is not allowed: each test should only mock what is required.

## Rationale

- The AAA pattern improves readability and maintainability of tests.
- JUnit 5 is widely adopted, has a rich ecosystem, and supports modern testing practices.
- AssertJ provides a fluent and expressive API for assertions, enhancing test clarity.
- jqwik allows for effective Property-Based Testing, which can uncover edge cases.
- Mockito is a well-established mocking framework that integrates seamlessly with JUnit.

## Consequences

Positive:

- Homogeneous test code across the codebase
- Easier onboarding for new developers
- Improved readability and maintainability of tests

Negative / Risks:

- Risk 1: Resistance to change from developers accustomed to other styles/libraries
  - Mitigation: Provide training and documentation on the new standards.
- Risk 2: Initial slowdown in test writing as developers adapt to the new format
  - Mitigation: Encourage pair programming and code reviews to facilitate knowledge sharing.

## Alternatives Considered

1. Use jupiter test methods
   - Pros: no need for a new external dependency for tests
   - Cons: less expressive assertions, more boilerplate code (eg: `Assertions.assertEquals(expected, actual)` vs
     `assertThat(actual).isEqualTo(expected)`)
2. Use TestNG as the test framework
   - Pros: more features out-of-the-box (eg: data providers)
   - Cons: less widely adopted in the Java community, less integration with other libraries
3. Use Hamcrest for assertions
   - Pros: widely used, part of JUnit ecosystem
   - Cons: less fluent API compared to AssertJ, more verbose syntax
4. Use Spock framework
   - Pros: very expressive, built-in mocking and stubbing
   - Cons: requires Groovy, which adds complexity to the Java codebase

## Implementation Plan

- [] Accept this ADR
- [] Use it for code reviews

## Open Issues

- None

## References

