package com.jdk.greeting.api;

/** Service interface (exported). */
public interface Greeter {

    String language();

    String greet(String name);
}
