# Sparrow release shrinking rules.
#
# Compose, DataStore, and Coroutines all ship their own consumer ProGuard
# rules, so this file only needs project-specific exceptions.

# Keep the overlay service and receiver class names stable so notification
# PendingIntents (which reference the service/receiver by explicit component
# name) keep resolving correctly after minification.
-keep class com.mahesh.sparrow.overlay.SparrowOverlayService { *; }
-keep class com.mahesh.sparrow.receiver.SparrowActionReceiver { *; }
