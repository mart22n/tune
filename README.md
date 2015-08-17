# tuneapp

An Android app for giving feedback how accurately you stay in tune when you sing or play an instrument. Suitable for anyone who thinks their "musical ear" might need some help or training.

#Features
The app shows in a great way, how the user has deviated from the intended tune, by

1) displaying notes separately, so that user can review, how he deviated during each individual note. A separate note is defined as a sound whose frequency is consistently in the (-50cent;+50cent) range from the expected note;

2) showing the user an alert when they have deviated more than a predefined value from the key the song was started in;

3) showing, using a color, how the user has deviated from the key the song started in. A color is visually easier to perceive than numeric or needle-based indicators

#Structure
The application's android studio project has 3 folders on top level:
app - that's where the app's code resides. Code is based on https://github.com/nivwusquorum/Simple-Guitar-Tuner
main - third-party speedometer display library which is copied from https://github.com/ntoskrnl/AndroidWidgets
MPAndroidChart - third-party Android charting library, licensed under Apache v2.0, copied from https://github.com/PhilJay/MPAndroidChart.

When it is clear the third-party libraries do not need modifications, we may reference them from online, so that tuneapp does not contain copies of the libraries' sources anymore.

Currently anyone is welcome to contribute!
