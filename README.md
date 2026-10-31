# SurfingCompose
An app to learn about Jetpack Compose and Github actions

Build with JDK 21 and an Android SDK containing platform 37 and build tools 36.0.0.
The project uses Gradle 9.8.2, Android Gradle Plugin 9.4.0, and Kotlin 2.4.21
(AGP built-in Kotlin with the matching Compose compiler plugin). AGP is pinned
to 9.4.0 for compatibility with Android Studio Rabbit 1 (2026.2.1). Dependencies
use stable releases verified on October 31, 2026. The minimum supported Android
version is API 24, as required by the updated AndroidX libraries; compile and
target SDK are 37 (Android 17).
Set `RAPID_API_KEY` in the environment, or add a quoted `RAPID_API_KEY` value to
your local `local.properties`. Environment values take precedence. Builds without
a key can run the tests and use cached recipes, but cannot fetch recipes online.

For GitHub Actions, configure the repository secret `RAPID_API_KEY`. All Gradle
jobs read it directly from the environment. Firebase jobs also require their
existing Firebase application and service account secrets.

Run `./gradlew testDebugUnitTest lintDebug assembleDebug` to validate the app.


Discovery loads 30 recipes at a time. “Load more recipes” preserves the current
search and cuisine, diet, and time filters; a failed next page can be retried
without clearing already loaded recipes. Preparation time and servings are shown
when available, with a step count for older cached recipes.

The recipe list becomes a grid from 520 dp of available pane width. Windows at
least 840 dp wide show the list and selected recipe side by side. Cooking keeps
step navigation and the active timer visible below the scrolling instructions.
Detail and cooking checkmarks share the recipe session and survive restoration.

Favorites, collections, and memberships are stored separately from discovery in
Room. Schema 5 migrates existing cache databases and imports the previous
`recipe_library` preferences once, on a background dispatcher. The preferences
are retained as a recovery copy and are no longer written. Collection rename and
favorite Undo preserve memberships; deleting a collection keeps saved recipes.
Navigation saves recipe IDs and reloads details from Room or the saved library.

Run `./gradlew connectedDebugAndroidTest` with an emulator for UI and migration
checks. `AdaptiveLayoutTest` can also run on a tablet; it verifies the two-pane
layout and writes review screenshots to the app cache using deterministic data.
