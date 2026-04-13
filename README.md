# MarkerBuilder

MarkerBuilder is now a modern Android sample and reusable library for building draggable geofence circles on Google Maps with Jetpack Compose and the latest Maps Compose API.

## What's new

- Android Gradle Plugin `8.13.2` with Gradle `8.14.4`
- Kotlin `2.2.21`
- Jetpack Compose UI app sample
- Google Maps Compose `8.3.0`
- Kotlin-first geofence state manager with unit tests
- Removed deprecated support libraries, legacy map fragments, and obsolete permissions

## Project structure

- `app`: a Compose demo app that renders circles and draggable handles with `GoogleMap`
- `geofencemarkerbuilder`: a reusable state manager and geometry utilities for draft and saved geofence circles

## Running the sample

1. Install Android SDK 36 or later.
2. Add a Google Maps key to `local.properties`:

```properties
MAPS_API_KEY=YOUR_API_KEY
```

3. Build the project:

```bash
./gradlew testDebugUnitTest assembleDebug
```

## Compose sample behavior

- Tap anywhere on the map to create a draft circle.
- Drag the center marker to move the circle.
- Drag the resize handle to change the radius.
- Seed saved circles with the bottom sheet action to test multiple overlays.
- Lock marker editing from the top bar to inspect without changing geometry.

## Library usage

```kotlin
val manager = MarkerBuilderManagerV2.Builder()
    .enabled(true)
    .radius(220.0)
    .minRadius(50)
    .maxRadius(1500)
    .build()

manager.markThis(point = LatLng(14.5995, 120.9842), isSaved = false)
manager.plotPoints(
    point = LatLng(14.6080, 120.9822),
    radius = 280,
    circularGeofenceId = 1001L,
    fillColor = 0x334CAF50.toInt(),
)
```

Collect `manager.uiState` from Compose or any other UI layer to render circles and marker handles.

## License

Copyright 2015 A-Ar Andrew Concepcion

Licensed under the Apache License, Version 2.0.
