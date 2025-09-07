# Only keep Compose classes if they exist
-if class androidx.compose.**
-keep class androidx.compose.** { *; }

# Only keep Koin classes if they exist
-if class org.koin.**
-keep class org.koin.** { *; }

# Only keep Koin annotated classes if the annotation exists
-if class org.koin.core.annotation.*
-keep @org.koin.core.annotation.* class * { *; }