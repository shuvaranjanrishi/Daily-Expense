# ১. আগের জেনারেট হওয়া রুলগুলো (Suppress Warnings)
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn java.awt.**
-dontwarn javax.xml.stream.**
-dontwarn net.sf.saxon.**
-dontwarn org.apache.batik.**
-dontwarn org.osgi.framework.**

# ২. Apache POI এবং অন্যান্য Excel লাইব্রেরির জন্য
-dontwarn org.apache.poi.**
-keep class org.apache.poi.** { *; }
-keep class org.apache.xmlbeans.** { *; }
-keep class javax.xml.stream.** { *; }

# ৩. R8 কে জোরপূর্বক নির্দেশ দেওয়া যাতে সে এরর না দেয়
-ignorewarnings
-keepattributes *Annotation*, Signature, InnerClasses
-dontnote **

# ৪. আপনার মডেল ক্লাসগুলো সুরক্ষিত রাখা (যদি থাকে)
-keep class com.therishideveloper.dailyexpense.models.** { *; }