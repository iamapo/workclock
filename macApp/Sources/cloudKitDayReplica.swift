import CloudKit
#if canImport(WorkClockCore)
import WorkClockCore
#elseif canImport(ComposeApp)
import ComposeApp
#endif

final class CloudKitDayReplica: NSObject, CloudReplica {
    static let containerIdentifier = "iCloud.com.iamapo.timetracker"
    static let recordType = "WorkDay"

    private static let payloadField = "payload"
    private static let updatedAtField = "updatedAtEpochMillis"

    private let database: CKDatabase
    private var isSubscriptionInstalled = false
    var onError: ((String) -> Void)?

    override init() {
        database = CKContainer(identifier: Self.containerIdentifier).privateCloudDatabase
        super.init()
    }

    func upload(
        records: [DayRecord],
        completionHandler: @escaping (Set<String>?, Error?) -> Void
    ) {
        let cloudRecords = records.map(Self.cloudRecord(from:))
        let operation = CKModifyRecordsOperation(recordsToSave: cloudRecords, recordIDsToDelete: nil)
        operation.savePolicy = .allKeys
        operation.isAtomic = false
        var uploadedDates = Set<String>()
        operation.perRecordSaveBlock = { recordID, result in
            switch result {
            case .success:
                uploadedDates.insert(recordID.recordName)
            case .failure(let error):
                DispatchQueue.main.async {
                    self.report(error, operation: "upload \(recordID.recordName)")
                }
            }
        }
        operation.modifyRecordsResultBlock = { result in
            DispatchQueue.main.async {
                switch result {
                case .success:
                    completionHandler(uploadedDates, nil)
                case .failure(let error):
                    self.report(error, operation: "upload")
                    completionHandler(nil, error)
                }
            }
        }
        database.add(operation)
    }

    func fetchAll(completionHandler: @escaping ([DayRecord]?, Error?) -> Void) {
        Task {
            do {
                // shortcut: fetches every day on each sync, switch to change tokens if it gets slow.
                let query = CKQuery(
                    recordType: Self.recordType,
                    predicate: NSPredicate(format: "\(Self.updatedAtField) >= 0")
                )
                var records: [DayRecord] = []
                var page = try await database.records(matching: query)
                records.append(contentsOf: try Self.dayRecords(from: page.matchResults))
                while let cursor = page.queryCursor {
                    page = try await database.records(continuingMatchFrom: cursor)
                    records.append(contentsOf: try Self.dayRecords(from: page.matchResults))
                }
                await installSubscriptionIfNeeded()
                let fetchedRecords = records
                await MainActor.run {
                    completionHandler(fetchedRecords, nil)
                }
            } catch let error as CKError where error.code == .unknownItem {
                // CloudKit creates the record type on the first upload.
                await MainActor.run {
                    completionHandler([], nil)
                }
            } catch {
                await MainActor.run {
                    self.report(error, operation: "download")
                    completionHandler(nil, error)
                }
            }
        }
    }

    private func installSubscriptionIfNeeded() async {
        guard !isSubscriptionInstalled else { return }
        do {
            try await installSubscription()
            isSubscriptionInstalled = true
        } catch {
            report(error, operation: "install subscription")
        }
    }

    private func installSubscription() async throws {
        let subscriptionID = "workclock-days-v1"
        do {
            _ = try await database.subscription(for: subscriptionID)
            return
        } catch let error as CKError where error.code == .unknownItem {
            // Create it below.
        }
        let subscription = CKQuerySubscription(
            recordType: Self.recordType,
            predicate: NSPredicate(value: true),
            subscriptionID: subscriptionID,
            options: [.firesOnRecordCreation, .firesOnRecordUpdate]
        )
        let notification = CKSubscription.NotificationInfo()
        notification.shouldSendContentAvailable = true
        subscription.notificationInfo = notification
        _ = try await database.save(subscription)
    }

    func report(_ error: Error, operation: String) {
        let message = "\(operation): \(error.localizedDescription)"
        print("[cloudkit-sync] \(message)")
        onError?(message)
    }

    private static func cloudRecord(from record: DayRecord) -> CKRecord {
        let cloudRecord = CKRecord(
            recordType: recordType,
            recordID: CKRecord.ID(recordName: record.date)
        )
        cloudRecord[payloadField] = record.payload as CKRecordValue?
        cloudRecord[updatedAtField] = NSNumber(value: record.updatedAtEpochMillis)
        return cloudRecord
    }

    private static func dayRecords(
        from results: [(CKRecord.ID, Result<CKRecord, Error>)]
    ) throws -> [DayRecord] {
        try results.compactMap { recordID, result in
            let cloudRecord = try result.get()
            guard let updatedAt = cloudRecord[updatedAtField] as? NSNumber else {
                return nil
            }
            return DayRecord(
                date: recordID.recordName,
                payload: cloudRecord[payloadField] as? String,
                updatedAtEpochMillis: updatedAt.int64Value
            )
        }
    }
}
