package com.tripexpense

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

class TripExpenseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(PersistentCacheSettings.newBuilder()
                .setSizeBytes(104857600L)
                .build())
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings
    }
}
