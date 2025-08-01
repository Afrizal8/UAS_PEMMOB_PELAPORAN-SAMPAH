package com.example.pelaporan_sampah.repository

import android.util.Log
import com.example.pelaporan_sampah.models.Report
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ReportRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val reportsCollection = firestore.collection("reports")

    companion object {
        private const val TAG = "ReportRepository"
    }

    suspend fun addReport(report: Report): Result<String> {
        return try {
            Log.d(TAG, "Adding report: ${report.jenisSampah}")

            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "User not authenticated")
                return Result.failure(Exception("User tidak terautentikasi"))
            }

            val reportWithUserId = report.copy(
                userId = currentUser.uid,
                id = reportsCollection.document().id
            )

            Log.d(TAG, "Saving report with ID: ${reportWithUserId.id}")
            reportsCollection.document(reportWithUserId.id).set(reportWithUserId).await()

            Log.d(TAG, "Report saved successfully")
            Result.success(reportWithUserId.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add report: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getUserReports(): Result<List<Report>> {
        return try {
            Log.d(TAG, "Getting user reports")

            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "User not authenticated")
                return Result.failure(Exception("User tidak terautentikasi"))
            }

            Log.d(TAG, "Querying reports for user: ${currentUser.uid}")

            // Try with orderBy first
            var querySnapshot = try {
                Log.d(TAG, "Trying query with orderBy...")
                reportsCollection
                    .whereEqualTo("userId", currentUser.uid)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Query with orderBy failed: ${e.message}")
                Log.d(TAG, "Trying fallback query without orderBy...")

                // Fallback: query without orderBy
                reportsCollection
                    .whereEqualTo("userId", currentUser.uid)
                    .get()
                    .await()
            }

            Log.d(TAG, "Query completed. Documents found: ${querySnapshot.documents.size}")

            val reports = querySnapshot.documents.mapNotNull { document ->
                try {
                    Log.d(TAG, "Processing document: ${document.id}")
                    val report = document.toObject(Report::class.java)?.copy(id = document.id)
                    Log.d(TAG, "Document data: ${report?.jenisSampah} - ${report?.status}")
                    report
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse document ${document.id}: ${e.message}", e)
                    null
                }
            }.sortedByDescending { it.timestamp.toDate() } // Sort on client side

            Log.d(TAG, "Successfully parsed ${reports.size} reports")
            Result.success(reports)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user reports: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getAllReports(): Result<List<Report>> {
        return try {
            Log.d(TAG, "Getting all reports")

            val querySnapshot = reportsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val reports = querySnapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(Report::class.java)?.copy(id = document.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse document ${document.id}: ${e.message}", e)
                    null
                }
            }

            Log.d(TAG, "Successfully retrieved ${reports.size} reports")
            Result.success(reports)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get all reports: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateReportStatus(reportId: String, status: String): Result<Unit> {
        return try {
            Log.d(TAG, "Updating report $reportId status to $status")

            reportsCollection.document(reportId)
                .update("status", status)
                .await()

            Log.d(TAG, "Report status updated successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update report status: ${e.message}", e)
            Result.failure(e)
        }
    }
}