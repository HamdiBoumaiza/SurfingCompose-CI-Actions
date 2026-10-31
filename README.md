# SurfingCompose
An app to learn about Jetpack Compose and Github actions

Build with JDK 17 and an Android SDK containing platform 34 and build tools 34.0.0.
Set `RAPID_API_KEY` in the environment, or add a quoted `RAPID_API_KEY` value to
your local `local.properties`. Environment values take precedence. Builds without
a key can run the tests and use cached recipes, but cannot fetch recipes online.

For GitHub Actions, configure the repository secret `RAPID_API_KEY`. All Gradle
jobs read it directly from the environment. Firebase jobs also require their
existing Firebase application and service account secrets.

Run `./gradlew testDebugUnitTest lintDebug assembleDebug` to validate the app.
