//
//  CoreDataStack.swift
//  Round Timer
//
//  Created by Ravil Khusainov on 23/10/2016.
//  Copyright © 2016 KHuR. All rights reserved.
//

import Foundation
import CoreData

class StorageHandler {
    let check: (String) -> Bool
    let callback: () -> ()
    init(check: @escaping (String) -> Bool, callback: @escaping () -> ()) {
        self.check = check
        self.callback = callback
    }
}

final class CoreDataStack {
    static let shared = CoreDataStack()
    
    fileprivate(set) var mainContext: NSManagedObjectContext!
    internal let queue: OperationQueue = {
        let queue = OperationQueue()
        queue.name = "CoreData Queue"
        return queue
    }()
    fileprivate var handlers = NSHashTable<StorageHandler>.weakObjects()
    
    func setup(_ name: String = "Database", userId: Int, completion: @escaping () -> ()) {
        guard let modelURL = Bundle.main.url(forResource: name, withExtension:"momd") else {
            fatalError("Error loading model from bundle")
        }
        guard let mom = NSManagedObjectModel(contentsOf: modelURL) else {
            fatalError("Error initializing mom from: \(modelURL)")
        }
        let psc = NSPersistentStoreCoordinator(managedObjectModel: mom)
        mainContext = NSManagedObjectContext(concurrencyType: .mainQueueConcurrencyType)
        mainContext.persistentStoreCoordinator = psc
        queue.maxConcurrentOperationCount = 1
        queue.addOperation {
            let docURL = FileManager.default.userDirectoryUrl(for: userId)
            let storeURL = docURL.appendingPathComponent(name + ".sqlite")
            do {
                let options = [NSMigratePersistentStoresAutomaticallyOption: true, NSInferMappingModelAutomaticallyOption: true]
                try psc.addPersistentStore(ofType: NSSQLiteStoreType, configurationName: nil, at: storeURL, options: options)
            } catch {
                fatalError("Error migrating store: \(error)")
            }
            DispatchQueue.main.async(execute: completion)
            self.queue.maxConcurrentOperationCount = 10
        }
    }
    
    func read<A: NSManagedObject>(with predicate: NSPredicate? = nil, sortetBy sortDescriptors: [NSSortDescriptor]? = nil) throws -> [A] where A: CoreDataObject {
        return try A.all(in: mainContext, with: predicate, sortetBy: sortDescriptors)
    }
    
    func write(completion: @escaping (NSManagedObjectContext) -> Void, fail:@escaping (Swift.Error) -> Void, after: @escaping () -> ()) {
        queue.addOperation {
            DispatchQueue.main.async {
                let context = NSManagedObjectContext(concurrencyType: .privateQueueConcurrencyType)
                context.parent = self.mainContext
                context.performAndWait({
                    completion(context)
                    do {
                        try context.save()
                    } catch {
                        fail(error)
                    }
                })
                self.mainContext.performAndWait({
                    do {
                        try self.save()
                        after()
                    } catch {
                        fail(error)
                    }
                })
            }
        }
    }
    
    func subscribe(handler: StorageHandler) {
        handler.callback()
        handlers.add(handler)
    }
    
    func unsubscribe(handler: StorageHandler) {
        handlers.remove(handler)
    }
    
    private func save() throws {
        let contains: (NSManagedObject, StorageHandler) -> Bool = { object, handler in
            return handler.check(String(describing: type(of: object)))
        }
        let subs = handlers.allObjects.filter { (subscryber) -> Bool in
            return self.mainContext.deletedObjects.contains(where: {contains($0, subscryber)}) ||
                self.mainContext.insertedObjects.contains(where: {contains($0, subscryber)}) ||
                self.mainContext.updatedObjects.contains(where: {contains($0, subscryber)})
            }
        try mainContext.save()
        subs.forEach{$0.callback()}
    }
}

extension CoreDataStack {
    enum Error: Swift.Error {
        case fetch
    }
}

protocol CoreDataObject {
    static var entityName: String { get }
}

extension CoreDataObject where Self: NSManagedObject {
    static var request: NSFetchRequest<Self> {
        return NSFetchRequest<Self>(entityName: entityName)
    }
    
    static func find(in context: NSManagedObjectContext, with predicate: NSPredicate) throws -> Self? {
        return try all(in: context, with: predicate).first
    }
    
    static func all(in context: NSManagedObjectContext, with predicate: NSPredicate? = nil, sortetBy sortDescriptors: [NSSortDescriptor]? = nil) throws -> [Self] {
        let request = self.request
        request.predicate = predicate
        request.sortDescriptors = sortDescriptors
        return try context.fetch(request)
    }
    
    static func create(in context: NSManagedObjectContext) -> Self {
        return NSEntityDescription.insertNewObject(forEntityName: Self.entityName, into: context) as! Self
    }
}

