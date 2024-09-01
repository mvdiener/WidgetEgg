# WidgetEgg

Android version of the iOS WidgetEgg app.

## Proto Files

The proto files are automatically generated via the gradle build script. However, if you are
using this project in Android Studio, there will likely be reference errors. In order to fix
this, go to Help -> Edit Custom Properties and add this value: `idea.max.intellisense.
filesize=10000`. The generated protobuf Java source file is too large for the default file size
and as a result, all of the generated protobuf Kotlin files will have errors.

##

WidgetEgg is an independent app and is not affiliated with or endorsed by Auxbrain, Inc. All
trademarks, service marks, and logos pertaining to Egg, Inc are the exclusive property of
Auxbrain, Inc. and are not included under this project's license.
