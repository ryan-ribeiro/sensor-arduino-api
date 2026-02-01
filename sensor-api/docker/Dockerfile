# (removed Dockerfile frontend syntax directive to avoid pulling external frontend image)

# Comments are provided throughout this file to help you get started.
# If you need more help, visit the Dockerfile reference guide at
# https://docs.docker.com/go/dockerfile-reference/

# Want to help us make this template better? Share your feedback here: https://forms.gle/ybq9Krt8jtBL3iCk7

################################################################################

FROM eclipse-temurin:21-jdk-jammy as base
WORKDIR /build
COPY --chmod=0755 mvnw mvnw
COPY .mvn/ .mvn/
RUN apt-get update -y && \
    apt-get install -y --no-install-recommends dos2unix && \
    dos2unix mvnw && \
    chmod +x mvnw && \
    rm -rf /var/lib/apt/lists/*

FROM base as test
WORKDIR /build
COPY ./src src/
COPY pom.xml pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw test

FROM base as deps21
WORKDIR /build
RUN --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=cache,target=/root/.m2 \
    ./mvnw dependency:go-offline -DskipTests

################################################################################

# Create a stage for resolving and downloading dependencies.
FROM eclipse-temurin:17-jdk-jammy as deps

WORKDIR /build

# Copy the mvnw wrapper with executable permissions.
COPY --chmod=0755 mvnw mvnw
COPY .mvn/ .mvn/
# Copy the project's pom so mvnw can run without bind-mounting the file (works
# more reliably across platforms such as Windows). Use a cache for Maven
# dependencies so subsequent builds are faster.
COPY pom.xml pom.xml
RUN --mount=type=cache,target=/root/.m2 apt-get update -y && \
    apt-get install -y --no-install-recommends dos2unix && \
    dos2unix mvnw && chmod +x mvnw && \
    ./mvnw dependency:go-offline -DskipTests && \
    rm -rf /var/lib/apt/lists/*

################################################################################

# Create a stage for building the application based on the stage with downloaded dependencies.
# This Dockerfile is optimized for Java applications that output an uber jar, which includes
# all the dependencies needed to run your app inside a JVM. If your app doesn't output an uber
# jar and instead relies on an application server like Apache Tomcat, you'll need to update this
# stage with the correct filename of your package and update the base image of the "final" stage
# use the relevant app server, e.g., using tomcat (https://hub.docker.com/_/tomcat/) as a base image.
FROM deps as package

WORKDIR /build

COPY . .
RUN --mount=type=cache,target=/root/.m2 apt-get update -y && \
    apt-get install -y --no-install-recommends dos2unix && \
    dos2unix mvnw && chmod +x mvnw && \
    ./mvnw package -DskipTests && \
    mv target/$(./mvnw help:evaluate -Dexpression=project.artifactId -q -DforceStdout)-$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout).jar target/app.jar && \
    rm -rf /var/lib/apt/lists/*


################################################################################

FROM package as development
WORKDIR /build
# Copy the built jar from the package stage and run it with debug options.
COPY --from=package /build/target/app.jar ./app.jar
ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000"
CMD [ "java", "-Dspring.profiles.active=postgres", "-jar", "app.jar" ]

################################################################################

# Create a new stage for running the application that contains the minimal
# runtime dependencies for the application. This often uses a different base
# image from the install or build stage where the necessary files are copied
# from the install stage.
#
# The example below uses eclipse-turmin's JRE image as the foundation for running the app.
# By specifying the "17-jre-jammy" tag, it will also use whatever happens to be the
# most recent version of that tag when you build your Dockerfile.
# If reproducibility is important, consider using a specific digest SHA, like
# eclipse-temurin@sha256:99cede493dfd88720b610eb8077c8688d3cca50003d76d1d539b0efc8cca72b4.
FROM eclipse-temurin:17-jre-jammy AS final

# Create a non-privileged user that the app will run under.
# See https://docs.docker.com/go/dockerfile-user-best-practices/
ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser
USER appuser

# Copy the executable from the "package" stage.
COPY --from=package build/target/app.jar app.jar

EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "app.jar" ]
