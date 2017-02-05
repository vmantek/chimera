# Spring Boot / jPOS integration 

### Configuration

jPOS can now read your Spring Boot configuration and use it to replace values in your deployment descriptors! It also tracks configuration modifications and redeploys affected descriptors.

### Efficient jPOS deployment

Spring Boot creates a single jar containing your entire application, which can be made an executable. Your deployables live inside this file, and are deployed at runtime, applying the supplied configuration. Therefore making upgrades a snap.

### Dependency Injection 

Chimera allows for any component instantiated by Q2 to be able to participate in container dependency injection (autowiring) through annotations.

### JPA Support

jPOS now integrates JPA and jPOS modules into a single JPA EntityManagerFactory, allowing us to use things like Spring Data JPA in jPOS .. even alongside traditional jPOS approach.
