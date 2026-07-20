package com.iamapo.timetracker.backup

import com.iamapo.timetracker.data.WorkClockBackupSerializer
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.temporaryDirectory
import platform.Foundation.writeToURL
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UniformTypeIdentifiers.UTTypeData
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosBackupFileController(
    private val presenter: () -> UIViewController?
) : BackupFileController {
    private var pendingExport: ((BackupFileResult) -> Unit)? = null
    private var pendingImport: ((BackupReadResult) -> Unit)? = null
    private var temporaryExportUrl: NSURL? = null
    private val pickerDelegate = IosDocumentPickerDelegate(this)

    override fun saveBackup(
        suggestedFileName: String,
        content: String,
        onResult: (BackupFileResult) -> Unit
    ) {
        if (pendingExport != null || pendingImport != null) {
            onResult(BackupFileResult.Failure)
            return
        }

        val presentingController = presenter()
        if (presentingController == null) {
            onResult(BackupFileResult.Failure)
            return
        }

        val url = NSFileManager.defaultManager.temporaryDirectory
            .URLByAppendingPathComponent(suggestedFileName)
        if (url == null) {
            onResult(BackupFileResult.Failure)
            return
        }

        val written = NSString.create(string = content).writeToURL(
            url = url,
            atomically = true,
            encoding = NSUTF8StringEncoding,
            error = null
        )
        if (!written) {
            onResult(BackupFileResult.Failure)
            return
        }

        pendingExport = onResult
        temporaryExportUrl = url
        val picker = UIDocumentPickerViewController(
            forExportingURLs = listOf(url),
            asCopy = true
        ).apply {
            delegate = pickerDelegate
        }
        presentingController.presentViewController(picker, animated = true, completion = null)
    }

    override fun openBackup(onResult: (BackupReadResult) -> Unit) {
        if (pendingExport != null || pendingImport != null) {
            onResult(BackupReadResult.Failure)
            return
        }

        val presentingController = presenter()
        if (presentingController == null) {
            onResult(BackupReadResult.Failure)
            return
        }

        pendingImport = onResult
        val picker = UIDocumentPickerViewController(
            forOpeningContentTypes = listOf(UTTypeData),
            asCopy = true
        ).apply {
            allowsMultipleSelection = false
            delegate = pickerDelegate
        }
        presentingController.presentViewController(picker, animated = true, completion = null)
    }

    internal fun onDocumentsPicked(didPickDocumentsAtURLs: List<*>) {
        val importCallback = pendingImport
        if (importCallback != null) {
            pendingImport = null
            val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
            if (url == null) {
                importCallback(BackupReadResult.Failure)
                return
            }
            importCallback(readBackup(url))
            return
        }

        pendingExport?.invoke(BackupFileResult.Success)
        pendingExport = null
        removeTemporaryExport()
    }

    internal fun onPickerCancelled() {
        pendingImport?.invoke(BackupReadResult.Cancelled)
        pendingImport = null
        pendingExport?.invoke(BackupFileResult.Cancelled)
        pendingExport = null
        removeTemporaryExport()
    }

    private fun readBackup(url: NSURL): BackupReadResult {
        val hasSecurityAccess = url.startAccessingSecurityScopedResource()
        return try {
            val content = NSString.create(
                contentsOfURL = url,
                encoding = NSUTF8StringEncoding,
                error = null
            )?.toString() ?: return BackupReadResult.Failure

            if (content.encodeToByteArray().size > WorkClockBackupSerializer.MaxBackupBytes) {
                BackupReadResult.Failure
            } else {
                BackupReadResult.Success(content)
            }
        } finally {
            if (hasSecurityAccess) url.stopAccessingSecurityScopedResource()
        }
    }

    private fun removeTemporaryExport() {
        temporaryExportUrl?.let { url ->
            NSFileManager.defaultManager.removeItemAtURL(url, error = null)
        }
        temporaryExportUrl = null
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class IosDocumentPickerDelegate(
    private val owner: IosBackupFileController
) : NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        owner.onDocumentsPicked(didPickDocumentsAtURLs)
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        owner.onPickerCancelled()
    }
}
