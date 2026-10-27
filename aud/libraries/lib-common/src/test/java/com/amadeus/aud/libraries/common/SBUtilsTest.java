package com.amadeus.aud.libraries.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.Test;

class SBUtilsTest {

  @Test
  void resetClearsStringBuilder() {
    StringBuilder sb = new StringBuilder("content");

    SBUtils.reset(sb);

    assertThat(sb).isEmpty();
  }

  @Test
  void resetClearsStringBuffer() {
    StringBuffer sb = new StringBuffer("content");

    SBUtils.reset(sb);

    assertThat(sb).isEmpty();
  }

  @Test
  void removeLastCharTrimsStringBuilder() {
    StringBuilder sb = new StringBuilder("a,b,");

    SBUtils.removeLastChar(sb);

    assertThat(sb).hasToString("a,b");
  }

  @Test
  void removeLastCharIgnoresEmptyStringBuilder() {
    StringBuilder sb = new StringBuilder();

    SBUtils.removeLastChar(sb);

    assertThat(sb).isEmpty();
  }

  @Test
  void removeLastCharTrimsStringBuffer() {
    StringBuffer sb = new StringBuffer("a,b,");

    SBUtils.removeLastChar(sb);

    assertThat(sb).hasToString("a,b");
  }

  @Test
  void removeLastCharIgnoresEmptyStringBuffer() {
    StringBuffer sb = new StringBuffer();

    SBUtils.removeLastChar(sb);

    assertThat(sb).isEmpty();
  }

  @Test
  void constructorIsNotInstantiable() throws NoSuchMethodException {
    Constructor<SBUtils> constructor = SBUtils.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    assertThatThrownBy(constructor::newInstance)
        .isInstanceOf(InvocationTargetException.class)
        .hasCauseInstanceOf(UnsupportedOperationException.class);
  }
}
