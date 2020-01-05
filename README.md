An attempt to create a full fledged CLEAN architecture android based project using for the view layer Google's new Android Jetpack Compose (in developer preview though)

Using:
* Android Architecture Components adapted to Compose Components lifecycle (ViewModel, LiveData)
* Glide for fetching images adapted to Compose
* Retrofit for network calls (not integrated yet - only repository logic is implemented and tested)
* Room for local caching (not integrated yet - only repository logic is implemented and tested)
* WorkManager to defer upsert requests, when network is available (not integrated yet - only repository logic is implemented and tested)
* [Simple Stack](https://github.com/Zhuinden/simple-stack) for navigation and death process persistence 
* Kotlin Coroutines and Flow

 [More about Android Jetpack Compose](https://developer.android.com/jetpack/compose)
 Note: for now coroutines-Flow is not working, due to a compiler bug. Bug filled [here](https://issuetracker.google.com/issues/144253995)