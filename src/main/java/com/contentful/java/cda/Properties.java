package com.contentful.java.cda;

/**
 * This class holds specific properties, used throughout the sdk, not accessible by mere mortals.
 */
public class Properties {
  /**
   * DO NOT CALL THIS CONSTRUCTOR
   *
   * Calling this constructor will result in an {@link AssertionError},
   * since no instances of this class shall exist.
   */
  private Properties() {
    throw new AssertionError();
  }

  static final String VERSION_NAME = "7.5.0-SNAPSHOT";

}
