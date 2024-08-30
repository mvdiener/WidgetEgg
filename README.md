# WidgetEgg

Android version of the iOS WidgetEgg app.

# Proto Files

The proto files are automatically generated via the gradle build script. However, if you are
using this project in Android Studio, there will likely be reference errors. In order to fix
this, go to Help -> Edit Custom Properties and add this value: `idea.max.intellisense.
filesize=10000`. The generated protobuf Java source file is too large for the default file size
and as a result, all of the generated protobuf Kotlin files will have errors.
