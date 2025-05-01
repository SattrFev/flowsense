# Main plugin class (keep everything)
-keep public class id.sattr.flowsense {
    *;
}

# Keep plugin lifecycle methods
-keepclassmembers class id.sattr.flowsense {
    public void onEnable();
    public void onDisable();
}

# Spigot/Bukkit API protection
-keep class org.bukkit.** { *; }
-keep class net.md_5.bungee.** { *; }

# Plugin metadata and commands
-keepattributes *Annotation*,Signature,Exceptions,InnerClasses
-keepclassmembers class * {
    @org.bukkit.event.EventHandler *;
}

# Configuration files (if using reflection)
-keepclassmembers class id.sattr.flowsense.** {
    @org.bukkit.configuration.file.YamlConfiguration *;
}

# Shaded libraries (adjust as needed)
-keep class io.github.milkdrinkers.colorparser.** { *; }

# Essential runtime attributes
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# Disable aggressive optimization
-dontoptimize
-dontshrink