//
//  CoreDataBacked.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 04.02.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation
import CoreData

protocol CoreDataBacked {
    associatedtype CD: CDObject
    static func transform(_ cd: CD) -> Self
    func transform(with context: NSManagedObjectContext) -> CD
}

protocol CDObject: NSManagedObject {
    associatedtype ID: Equatable
    var id: Self.ID { get }
    static var entityName: String { get }
}

struct CoreDataStorage {
    
    static var shared: CoreDataStorage?
        
    fileprivate let mainContext: NSManagedObjectContext
    fileprivate let queue = OperationQueue()
    
    init(_ bundle: Bundle = .main, name: String, directory: URL) {
        guard let modelURL = bundle.url(forResource: name, withExtension:"momd") else {
            fatalError("Error loading model from bundle")
        }
        guard let mom = NSManagedObjectModel(contentsOf: modelURL) else {
            fatalError("Error initializing mom from: \(modelURL)")
        }
        let psc = NSPersistentStoreCoordinator(managedObjectModel: mom)
        mainContext = NSManagedObjectContext(concurrencyType: .mainQueueConcurrencyType)
        mainContext.persistentStoreCoordinator = psc
        mainContext.mergePolicy = NSOverwriteMergePolicy
        
        queue.name = "CoreData.Queue"
        queue.isSuspended = true
        queue.maxConcurrentOperationCount = 10
        
        let storeURL = directory.appendingPathComponent(name + ".sqlite")
        do {
            let options = [NSMigratePersistentStoresAutomaticallyOption: true, NSInferMappingModelAutomaticallyOption: true]
            try psc.addPersistentStore(ofType: NSSQLiteStoreType, configurationName: nil, at: storeURL, options: options)
        } catch {
            fatalError("Error migrating store: \(error)")
        }
        queue.isSuspended = false
    }
    
    func read<Model: CDObject>(predicate: NSPredicate? = nil, sortetBy sortDescriptors: [NSSortDescriptor]? = nil) throws -> [Model] {
        let request = NSFetchRequest<Model>(entityName: Model.entityName)
        request.predicate = predicate
        request.sortDescriptors = sortDescriptors
        return try mainContext.fetch(request)
    }
    
    func write(completion: (NSManagedObjectContext) -> ()) {
        let context = NSManagedObjectContext(concurrencyType: .privateQueueConcurrencyType)
        context.parent = self.mainContext
        context.mergePolicy = NSOverwriteMergePolicy
        context.performAndWait({
            completion(context)
            do {
                try context.save()
            } catch {
//                fail(error)
            }
        })
        self.mainContext.performAndWait({
            do {
                try self.mainContext.save()
//                let hasChanges = try self.save()
//                after(hasChanges)
            } catch {
//                fail(error)
            }
        })
    }
}

@propertyWrapper
struct CoreDataSubscriber<Model: CoreDataBacked> {
    let storage: CoreDataStorage?
    init(storage: CoreDataStorage? = .shared) {
        self.storage = storage
    }
    var wrappedValue: CoreDataObserver<Model> {
        let observer = CoreDataObserver<Model>(storage: storage)
        return observer
    }
}

class CoreDataObserver<Model: CoreDataBacked> {
    let storage: CoreDataStorage!
    let queue: DispatchQueue
    init(storage: CoreDataStorage!, queue: DispatchQueue = .main) {
        self.storage = storage
        self.queue = queue
    }
    
    func get<T: Equatable>(by keyPath: KeyPath<Model, T>, value: T, completion: @escaping (Model) -> ()) throws {
        try checkStorage()
        let result: [Model.CD] = try storage.read()
        if let item = result.map(Model.transform).first(where: {$0[keyPath: keyPath] == value}) {
            queue.async {
                completion(item)
            }
        }
    }
    
    func all(completion: @escaping ([Model]) -> ()) throws {
        try checkStorage()
        let result: [Model.CD] = try storage.read()
        if !result.isEmpty {
            queue.async {
                completion(result.map(Model.transform))
            }
        }
    }
    
    func save(_ models: Model...) throws {
        try checkStorage()
        storage.write { (context) in
            _ = models.map{$0.transform(with: context)}
        }
    }
    
    fileprivate func checkStorage() throws {
        guard storage != nil else { throw CoreDataError.storageIsNil }
    }
}

enum CoreDataError: Error {
    case storageIsNil
}
