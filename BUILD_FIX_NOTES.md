# Build fixes applied

- Removed `@Transactional` from the three forgot-password methods (`sendOtp`, `verifyOtp`, `resetPassword`) in the five role services. These methods do not require an explicit transaction and Spring Boot 4 compilation was rejecting the annotation in this source.
- Configured Lombok explicitly in Maven (version 1.18.38) and registered it as a compiler annotation processor so generated getters/setters/constructors are available during compilation.

After extraction, from the folder containing `pom.xml`, run:

`mvn clean package -DskipTests`

If using the wrapper on Windows:

`./mvnw.cmd clean package -DskipTests`
