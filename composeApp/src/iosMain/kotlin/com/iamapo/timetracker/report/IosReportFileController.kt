package com.iamapo.timetracker.report

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
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosReportFileController(
    private val presenter: () -> UIViewController?
) : ReportFileController {
    private var pendingExport: ((ReportFileResult) -> Unit)? = null
    private var temporaryExportUrl: NSURL? = null
    private val pickerDelegate = IosReportDocumentPickerDelegate(this)

    override fun saveReport(
        suggestedFileName: String,
        pdfContent: String,
        onResult: (ReportFileResult) -> Unit
    ) {
        if (pendingExport != null) {
            onResult(ReportFileResult.Failure)
            return
        }
        val presentingController = presenter()
        if (presentingController == null) {
            onResult(ReportFileResult.Failure)
            return
        }

        val url = NSFileManager.defaultManager.temporaryDirectory
            .URLByAppendingPathComponent(suggestedFileName)
        if (url == null) {
            onResult(ReportFileResult.Failure)
            return
        }
        val written = NSString.create(string = pdfContent).writeToURL(
            url = url,
            atomically = true,
            encoding = NSUTF8StringEncoding,
            error = null
        )
        if (!written) {
            onResult(ReportFileResult.Failure)
            return
        }

        pendingExport = onResult
        temporaryExportUrl = url
        val picker = UIDocumentPickerViewController(
            forExportingURLs = listOf(url),
            asCopy = true
        ).apply { delegate = pickerDelegate }
        presentingController.presentViewController(picker, animated = true, completion = null)
    }

    internal fun onDocumentsPicked() {
        pendingExport?.invoke(ReportFileResult.Success)
        pendingExport = null
        removeTemporaryExport()
    }

    internal fun onPickerCancelled() {
        pendingExport?.invoke(ReportFileResult.Cancelled)
        pendingExport = null
        removeTemporaryExport()
    }

    private fun removeTemporaryExport() {
        temporaryExportUrl?.let { url ->
            NSFileManager.defaultManager.removeItemAtURL(url, error = null)
        }
        temporaryExportUrl = null
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class IosReportDocumentPickerDelegate(
    private val owner: IosReportFileController
) : NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        owner.onDocumentsPicked()
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        owner.onPickerCancelled()
    }
}
