package com.iamapo.timetracker.backup

import com.iamapo.timetracker.data.WorkClockBackupSerializer
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.BooleanVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSURL
import platform.Foundation.NSUserDefaults
import platform.Foundation.create
import platform.Foundation.temporaryDirectory
import platform.Foundation.writeToURL
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UniformTypeIdentifiers.UTTypeData
import platform.UniformTypeIdentifiers.UTTypeFolder
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosBackupFileController(
    private val presenter: () -> UIViewController?
) : BackupFileController {
    private var pendingExport: ((BackupFileResult) -> Unit)? = null
    private var pendingImport: ((BackupReadResult) -> Unit)? = null
    private var pendingFolderChoice: ((BackupFileResult) -> Unit)? = null
    private val defaults = NSUserDefaults.standardUserDefaults
    private var temporaryExportUrl: NSURL? = null
    private val pickerDelegate = IosDocumentPickerDelegate(this)

    override fun saveBackup(
        suggestedFileName: String,
        content: String,
        onResult: (BackupFileResult) -> Unit
    ) {
        if (isPickerOpen) {
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
        if (isPickerOpen) {
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

    override fun hasAutoBackupFolder(): Boolean = defaults.dataForKey(FOLDER_BOOKMARK_KEY) != null

    override fun chooseAutoBackupFolder(onResult: (BackupFileResult) -> Unit) {
        if (isPickerOpen) {
            onResult(BackupFileResult.Failure)
            return
        }

        val presentingController = presenter()
        if (presentingController == null) {
            onResult(BackupFileResult.Failure)
            return
        }

        pendingFolderChoice = onResult
        val picker = UIDocumentPickerViewController(
            forOpeningContentTypes = listOf(UTTypeFolder),
            asCopy = false
        ).apply {
            allowsMultipleSelection = false
            delegate = pickerDelegate
        }
        presentingController.presentViewController(picker, animated = true, completion = null)
    }

    override fun clearAutoBackupFolder() {
        defaults.removeObjectForKey(FOLDER_BOOKMARK_KEY)
    }

    override fun listAutoBackupFiles(): List<String>? = withAutoBackupFolder { folder ->
        // shortcut: evicted iCloud files can appear as ".name.icloud" placeholders and are then
        // not rotated; only leftover old files, handle them if a folder ever grows noticeably.
        folder.path
            ?.let { path ->
                NSFileManager.defaultManager.contentsOfDirectoryAtPath(path, error = null)
            }
            ?.filterIsInstance<String>()
    }

    override fun writeAutoBackup(fileName: String, content: String): Boolean =
        withAutoBackupFolder { folder ->
            val url = folder.URLByAppendingPathComponent(fileName) ?: return@withAutoBackupFolder false
            // shortcut: no NSFileCoordinator, add it if cloud providers show conflicting copies.
            NSString.create(string = content).writeToURL(
                url = url,
                atomically = true,
                encoding = NSUTF8StringEncoding,
                error = null
            )
        } ?: false

    override fun deleteAutoBackupFile(fileName: String) {
        withAutoBackupFolder { folder ->
            folder.URLByAppendingPathComponent(fileName)?.let { url ->
                NSFileManager.defaultManager.removeItemAtURL(url, error = null)
            }
        }
    }

    internal fun onDocumentsPicked(didPickDocumentsAtURLs: List<*>) {
        val folderCallback = pendingFolderChoice
        if (folderCallback != null) {
            pendingFolderChoice = null
            val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
            val saved = url != null && saveFolderBookmark(url)
            folderCallback(if (saved) BackupFileResult.Success else BackupFileResult.Failure)
            return
        }

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
        pendingFolderChoice?.invoke(BackupFileResult.Cancelled)
        pendingFolderChoice = null
        pendingImport?.invoke(BackupReadResult.Cancelled)
        pendingImport = null
        pendingExport?.invoke(BackupFileResult.Cancelled)
        pendingExport = null
        removeTemporaryExport()
    }

    private val isPickerOpen: Boolean
        get() = pendingExport != null || pendingImport != null || pendingFolderChoice != null

    private fun saveFolderBookmark(url: NSURL): Boolean {
        val hasSecurityAccess = url.startAccessingSecurityScopedResource()
        return try {
            storeBookmark(url)
        } finally {
            if (hasSecurityAccess) url.stopAccessingSecurityScopedResource()
        }
    }

    private fun storeBookmark(url: NSURL): Boolean {
        val bookmark: NSData = url.bookmarkDataWithOptions(
            options = 0u,
            includingResourceValuesForKeys = null,
            relativeToURL = null,
            error = null
        ) ?: return false

        defaults.setObject(bookmark, forKey = FOLDER_BOOKMARK_KEY)
        return true
    }

    private fun <T> withAutoBackupFolder(action: (NSURL) -> T): T? {
        val bookmark = defaults.dataForKey(FOLDER_BOOKMARK_KEY) ?: return null

        return memScoped {
            val isStale = alloc<BooleanVar>()
            val folder = NSURL.URLByResolvingBookmarkData(
                bookmark,
                options = 0u,
                relativeToURL = null,
                bookmarkDataIsStale = isStale.ptr,
                error = null
            ) ?: return null

            val hasSecurityAccess = folder.startAccessingSecurityScopedResource()
            try {
                if (isStale.value) storeBookmark(folder)
                action(folder)
            } finally {
                if (hasSecurityAccess) folder.stopAccessingSecurityScopedResource()
            }
        }
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

    private companion object {
        const val FOLDER_BOOKMARK_KEY = "auto_backup_folder_bookmark"
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
