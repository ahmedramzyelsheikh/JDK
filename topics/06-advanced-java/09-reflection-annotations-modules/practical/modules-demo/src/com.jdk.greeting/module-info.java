/**
 * Provider module: exports only its API package and provides two Greeter implementations as services.
 */
module com.jdk.greeting {
    exports com.jdk.greeting.api;                       // visible to other modules
    // com.jdk.greeting.internal is NOT exported: hidden even though its classes are public

    provides com.jdk.greeting.api.Greeter
            with com.jdk.greeting.internal.EnglishGreeter,
                 com.jdk.greeting.internal.ArabicGreeter;
}
