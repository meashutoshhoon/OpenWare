package jb.openware.app.util

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class FirebaseUtils {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /* -------------------------------- Storage -------------------------------- */

    fun uploadFileToStorage(
        reference: String,
        child: String,
        fileUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val fileRef = storage.getReference(reference).child(child)

        fileRef.putFile(fileUri).continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                fileRef.downloadUrl
            }.addOnSuccessListener { uri -> onSuccess(uri.toString()) }
            .addOnFailureListener(onFailure)
    }

    fun deleteFileFromStorageByUrl(vararg urls: String?) {
        val storage = FirebaseStorage.getInstance()

        urls
            .filterNotNull()
            .filter { it.isNotBlank() && it != "none" }
            .forEach { url ->
                runCatching {
                    storage.getReferenceFromUrl(url).delete()
                }.onFailure {
                     Log.e("FirebaseUtils", "Failed to delete file: $url", it)
                }
            }
    }


    /* -------------------------------- Database -------------------------------- */

    fun getData(
        reference: String, child: String, callback: (Map<String, Any>?) -> Unit
    ) {
        database.getReference(reference).child(child)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        callback(null)
                        return
                    }

                    val data =
                        snapshot.getValue(object : GenericTypeIndicator<Map<String, Any>>() {})
                    callback(data)
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
        getData("Users", uid) { data ->
            val current = data?.get(key)?.toString()?.toIntOrNull() ?: return@getData
            val updated = (current + delta).coerceAtLeast(0)

            pushToDatabase(
                mapOf(key to updated.toString()),
                "Users",
                uid,
                onSuccess = {},
                onFailure = {})
        }
    }

    /* -------------------------------- Auth -------------------------------- */

    fun getUid(): String = auth.currentUser?.uid ?: error("User not authenticated")
}