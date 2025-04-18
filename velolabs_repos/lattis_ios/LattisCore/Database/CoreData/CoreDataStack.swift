//
//  CoreDataStack.swift
//  Round Timer
//
//  Created by Ravil Khusainov on 23/10/2016.
//  Copyright © 2016 KHuR. All rights reserved.
//

import Foundation
import CoreData

final class CoreDataStack {
    static let shared = CoreDataStack()
    
    var isReady: Bool {
        return mainContext != nil
    }
    fileprivate(set) var mainContext: NSManagedObjectContext!
    fileprivate let queue: OperationQueue = {
        let queue = OperationQueue()
        queue.name = "CoreData Queue"
        queue.isSuspended = true
        return queue
    }()
    fileprivate var storage = NSHashTable<AnyObject>.weakObjects()
    fileprivate func subscrybers() -> [CoreDataSub] {
        guard let subs = storage.allObjects as? [CoreDataSub] else { return [] }
        return subs
    }
    
    func setup(_ bundle: Bundle = .main, name: String, userId: Int, completion: @escaping () -> ()) {
        guard let modelURL = bundle.url(forResource: name, withExtension:"momd") else {
            fatalError("Error loading model from bundle")
        }
        guard let mom = NSManagedObjectModel(contentsOf: modelURL) else {
            fatalError("Error initializing mom from: \(modelURL)")
        }
        let psc = NSPersistentStoreCoordinator(managedObjectModel: mom)
        mainContext = NSManagedObjectContext(concurrencyType: .mainQueueConcurrencyType)
        mainContext.persistentStoreCoordinator = psc
        queue.maxConcurrentOperationCount = 1
        let operation = BlockOperation {
            let docURL = FileManager.default.userDirectoryUrl(for: userId)
            let storeURL = docURL.appendingPathComponent(name + ".sqlite")
            do {
                let options = [NSMigratePersistentStoresAutomaticallyOption: true, NSInferMappingModelAutomaticallyOption: true]
                try psc.addPersistentStore(ofType: NSSQLiteStoreType, configurationName: nil, at: storeURL, options: options)
                DispatchQueue.main.async(execute: completion)
            } catch {
                fatalError("Error migrating store: \(error)")
            }
            self.queue.maxConcurrentOperationCount = 10
        }
        operation.queuePriority = .veryHigh
        queue.addOperation(operation)
        queue.isSuspended = false
    }
    
    func read<A: NSManagedObject>(with predicate: NSPredicate? = nil, sortetBy sortDescriptors: [NSSortDescriptor]? = nil) throws -> [A] where A: CoreDataObject {
        return try A.all(in: mainContext, with: predicate, sortetBy: sortDescriptors)
    }
    
    func write(completion: @escaping (NSManagedObjectContext) -> Void, fail:@escaping (Swift.Error) -> Void, after: @escaping (Bool) -> () = {_ in}) {
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
                        let hasChanges = try self.save()
                        after(hasChanges)
                    } catch {
                        fail(error)
                    }
                })
            }
        }
    }
    
    func subscribe<A: NSManagedObject>(with predicate: NSPredicate? = nil, completion: @escaping ([A]) -> ()) -> Subscriber<A> where A: CoreDataObject {
        let sub = Subscriber<A>(call: {
            self.queue.addOperation {
                DispatchQueue.main.async {
                    do {
                        let result: [A] = try self.read(with: predicate)
                        completion(result)
                    } catch {
                        print(error)
                        completion([])
                    }
                }
            }
        })
        sub.call()
        storage.add(sub)
        return sub
    }
    
    private func save() throws -> Bool {
        let contains: (NSManagedObject, CoreDataSub) -> Bool = { object, sub in
            return String(describing: type(of: object)) == String(describing: sub.type)
        }
        let subs = Array(subscrybers().filter { (subscryber) -> Bool in
            return self.mainContext.deletedObjects.contains(where: {contains($0, subscryber)}) ||
                self.mainContext.insertedObjects.contains(where: {contains($0, subscryber)}) ||
                self.mainContext.updatedObjects.contains(where: {contains($0, subscryber)})
        })
        let hasChanges = !mainContext.deletedObjects.isEmpty || !mainContext.insertedObjects.isEmpty || !mainContext.updatedObjects.isEmpty
        try mainContext.save()
        subs.forEach{$0.call()}
        return hasChanges
    }
}

// Convenience protocol
fileprivate protocol CoreDataSub {
    var type: NSManagedObject.Type {get}
    var call: () -> () {get}
}

extension CoreDataStack {
    enum Error: Swift.Error {
        case fetch
    }
    
    class Subscriber<A: NSManagedObject>: NSObject, CoreDataSub {
        init(call: @escaping () -> ()) {
            self.call = call
            self.type = A.self
        }
        let type: NSManagedObject.Type
        let call: () -> ()
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

