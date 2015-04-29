# MarkerBuilder
Versatile map area / geofence utility for google maps v2 for android.
Create geofence areas in your map by initializing markerBuilderManager.
Not that difficult isn't it?

Usage
------
```java
private void setUpMap() {
  markerBuilderManager = new MarkerBuilderManagerV2.Builder(this)
          .map(mMap) // required
          .build();
}
```

You can also set other marker options such as:
```java
  markerBuilderManager = new MarkerBuilderManagerV2.Builder(this)
          .map(googleMap)
          .enabled(isEnabled)
          .radius(initRadiusMetersFinal)
          .circleId(circleId)
          .strokeWidth(strokeWidth)
          .strokeColor(strokeColor)
          .fillColor(fillColor)
          .minRadius(minRadius)
          .maxRadius(maxRadius)
          .centerIcon(centerIcon)
          .centerBitmap(centerBitmap)
          .resizerIcon(resizerIcon)
          .centerOffsetHorizontal(centerOffsetHorizontal)
          .centerOffsetVertical(centerOffsetVertical)
          .build();
```

Configuration
------
There is no pre-configuration needed. :)

Download
------
Add the following code to your `build.gradle` file (as described on [JitPack])
```
repositories {
    maven {
        url "https://jitpack.io"
    }
}
dependencies {
    compile 'com.github.ac-opensource:MarkerBuilder:v1.0.0'
}
```

License
-------

    Copyright 2015 A-Ar Andrew Concepcion

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    
[JitPack]:https://jitpack.io/#ac-opensource/MarkerBuilder/v1.0.0
