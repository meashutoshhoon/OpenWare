package jb.openware.app.util

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import jb.openware.app.ui.items.UserProfile

class FirebaseUtils {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    fun deleteFileFromStorageByUrl(vararg urls: String?) {
        val storage = FirebaseStorage.getInstance()

        urls.filterNotNull().filter { it.isNotBlank() && it != "none" }.forEach { url ->
                runCatching {
                    storage.getReferenceFromUrl(url).delete()
                }.onFailure {
                    Log.e("FirebaseUtils", "Failed to delete file: $url", it)
                }
            }
    }


    /* -------------------------------- Database -------------------------------- */

    fun getUser(
        reference: String,
        child: String,
        callback: (UserProfile?) -> Unit
    ) {
        database.getReference(reference)
            .child(child)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(UserProfile::class.java)
                    callback(user)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null)
                }
            })
    }

    fun pushToDatabase(
        data: Map<String, Any>,
        reference: String,
        child: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        database.getReference(reference).child(child).updateChildren(data)
            .addOnSuccessListener { onSuccess() }.addOnFailureListener(onFailure)
    }

    /* ---------------------------- User counters ---------------------------- */

    fun increaseUserKeyData(key: String, uid: String) {
        updateUserCounter(key, uid, +1)
    }

    fun decreaseUserKeyData(key: String, uid: String) {
        updateUserCounter(key, uid, -1)
    }

    private fun updateUserCounter(key: String, uid: String, delta: Int) {
        getUser("Users", uid) { user ->
            var current = 0
            if (key == "likes") {
                current = user?.likes.toString().toIntOrNull() ?: return@getUser
            }
            val updated = (current + delta).coerceAtLeast(0)

            pushToDatabase(
                mapOf(key to updated.toString()),
                "Users",
                uid,
                onSuccess = {},
                onFailure = {})
        }
    }
}