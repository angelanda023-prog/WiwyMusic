package com.wiwymusic.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.wiwymusic.BuildConfig
import java.io.File
import java.security.MessageDigest

/** Verifica una OTA antes de entregarla al instalador del sistema. */
internal object ApkUpdateVerifier {
    private const val RELEASE_CERTIFICATE_SHA256 =
        "54165311e546cc7772dc48f059848b5aa5256250b8f6485fcc5b2abae0e8cb70"

    fun verify(context: Context, apk: File) {
        val packageManager = context.packageManager
        val candidate = packageManager.archivePackageInfo(apk)
            ?: throw IllegalStateException("La actualización descargada no es una APK válida")

        if (candidate.packageName != context.packageName) {
            throw IllegalStateException("La actualización no pertenece a WiwyMusic")
        }

        val installed = packageManager.installedPackageInfo(context.packageName)
        val installedSigners = installed.signerCertificates()
        val candidateSigners = candidate.signerCertificates()

        if (!sameSignerDigests(installedSigners, candidateSigners)) {
            throw IllegalStateException("La actualización no tiene la firma oficial de WiwyMusic")
        }
        if (!BuildConfig.DEBUG && !containsReleaseSigner(candidateSigners)) {
            throw IllegalStateException("La actualización no tiene la firma oficial de WiwyMusic")
        }
    }

    fun isOfficialInstalledApp(context: Context): Boolean {
        if (BuildConfig.DEBUG) return true
        return runCatching {
            val installed = context.packageManager.installedPackageInfo(context.packageName)
            containsReleaseSigner(installed.signerCertificates())
        }.getOrDefault(false)
    }

    internal fun sameSignerDigests(
        installedCertificates: List<ByteArray>,
        candidateCertificates: List<ByteArray>,
    ): Boolean {
        if (installedCertificates.isEmpty() || candidateCertificates.isEmpty()) return false
        return installedCertificates.toDigestSet() == candidateCertificates.toDigestSet()
    }

    internal fun containsReleaseSigner(certificates: List<ByteArray>): Boolean =
        RELEASE_CERTIFICATE_SHA256 in certificates.toDigestSet()

    @Suppress("DEPRECATION")
    private fun PackageManager.archivePackageInfo(apk: File): PackageInfo? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getPackageArchiveInfo(
                apk.absolutePath,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong()),
            )
        } else {
            getPackageArchiveInfo(apk.absolutePath, PackageManager.GET_SIGNING_CERTIFICATES)
        }

    @Suppress("DEPRECATION")
    private fun PackageManager.installedPackageInfo(packageName: String): PackageInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong()),
            )
        } else {
            getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        }

    @Suppress("DEPRECATION")
    private fun PackageInfo.signerCertificates(): List<ByteArray> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            signingInfo?.apkContentsSigners.orEmpty().map { it.toByteArray() }
        } else {
            signatures.orEmpty().map { it.toByteArray() }
        }

    private fun List<ByteArray>.toDigestSet(): Set<String> = mapTo(mutableSetOf()) { certificate ->
        MessageDigest.getInstance("SHA-256")
            .digest(certificate)
            .joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}
