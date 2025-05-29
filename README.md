# WidgetEgg

## About The App

This is the Android version of the iOS WidgetEgg app. Check out the original iOS app on
the [App Store](https://apps.apple.com/us/app/widgetegg/id6502221824).

Check out the Android version of the app on
the [Play Store](https://play.google.com/store/apps/details?id=com.widgetegg.widgeteggapp).

The Android version of this app was made with permission from the developer of the iOS app, to mimic
the design, layout, and functionality of the iOS app.

## Running The App Locally

Android Studio is recommended for running the app locally.

### Proto Files

The proto files are automatically generated via the gradle build script. However, if you are
using this project in Android Studio, there will likely be reference errors. In order to fix
this, go to Help -> Edit Custom Properties and add this value:
`idea.max.intellisense.filesize=10000`. The generated protobuf Java source file is too large for the
default file size and as a result, all of the generated protobuf Kotlin files will have errors.

### API Calls

In order to make API calls via the app, you will need to add a `secrets.properties` file to the root
of your project. Within this file add the following key/value pairs with the correct values.

```
SECRET_KEY=value_here
DEV_ACCOUNT=value_here
```

## Contributing

If you are interested in contributing or making feature requests, feel free to open a PR and/or come
find me on the [Egg Inc Discord](https://discord.gg/egginc).

## Disclaimer

WidgetEgg is an independent app and is not affiliated with or endorsed by Auxbrain, Inc. All
trademarks, service marks, and logos pertaining to Egg, Inc are the exclusive property of
Auxbrain, Inc. and are not included under this project's license.
