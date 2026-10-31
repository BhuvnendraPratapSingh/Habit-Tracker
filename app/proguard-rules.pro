# HabitFlow ProGuard Rules

# ── Room ─────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# BUG-17 FIX: Keep ALL fields in @Entity classes.
# Room maps Kotlin field names to SQLite column names.
# If R8 renames them, column lookups fail at runtime.
-keepclassmembers @androidx.room.Entity class * { *; }

# Keep Room Dao interfaces — Room generates implementations at compile time
-keep @androidx.room.Dao class * { *; }

-dontwarn androidx.room.paging.**

# ── Hilt ─────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# ── Kotlin ───────────────────────────────────────────────────────
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# ── Coroutines ───────────────────────────────────────────────────
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ── Compose ──────────────────────────────────────────────────────
-dontwarn androidx.compose.**

# BUG-16 FIX: Keep Compose text/font classes used by typography.
# Prevents R8 from stripping font weight/family resolution.
-keep class androidx.compose.ui.text.font.** { *; }
-keep class androidx.compose.ui.text.googlefonts.** { *; }

# ── Gson (used for data export) ──────────────────────────────────
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class com.habitflow.app.data.db.entity.** { *; }
-keep class com.habitflow.app.data.model.** { *; }
