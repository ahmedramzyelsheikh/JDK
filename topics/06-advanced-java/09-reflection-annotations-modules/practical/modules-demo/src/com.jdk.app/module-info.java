/**
 * Consumer module: reads the greeting module and uses its service.
 */
module com.jdk.app {
    requires com.jdk.greeting;

    uses com.jdk.greeting.api.Greeter;      // declares that we look up Greeter implementations
}
