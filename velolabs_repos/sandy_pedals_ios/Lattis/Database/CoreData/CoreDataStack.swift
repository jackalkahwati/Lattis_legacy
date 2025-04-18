//
//  CoreDataStack.swift
//  Round Timer
//
//  Created by Ravil Khusainov on 23/10/2016.
//  Copyright © 2016 KHuR. All rights reserved.
//

import Foundation
import CoreData

public extension URL {

    /// Returns a URL for the given app group and database pointing to the sqlite database.
    static func storeURL(for appGroup: String, databaseName: String) -> URL? {
        guard let fileContainer = FileManager.default.containerURL(forSecurityApplicationGroupIdentifier: appGroup) else {
            return nil
        }

        return fileContainer.appendingPathComponent("\(databaseName).sqlite")
    }
}

final class CoreDataStack {
    static let shared = CoreDataStack()
    
    fileprivate(set) var mainContext: NSManagedObjectContext!
    fileprivate let queue: OperationQueue = {
        let queue = OperationQueue()
        queue.name = "CoreData Queue"
        return queue
    }()
    fileprivate var subscrybers: [AnyHashable: Subscriber] = [:]
    
    func setup(_ name: String = "Lattis", userId: Int, completion: @escaping () -> ()) {
//        guard let modelURL = URL.storeURL(for: "group.io.lattis.www.Lattis", databaseName: name) else {
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
    
    func write(completion: @escaping (NSManagedObjectContext) -> Void, fail:@escaping (Swift.Error) -> Void, after: @escaping () -> () = {}) {
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
    
    func subscribe<A: NSManagedObject>(target: AnyHashable, with predicate: NSPredicate? = nil, completion: @escaping ([A]) -> ()) where A: CoreDataObject {
        let sub = Subscriber(type: A.self, call: {
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
        subscrybers[target] = sub
    }
    
    func unsubscribe(target: AnyHashable) {
        subscrybers[target] = nil
    }
    
    private func save() throws {
        let contains: (NSManagedObject, Subscriber) -> Bool = { object, sub in
            return String(describing: type(of: object)) == String(describing: sub.type)
        }
        let subs = Array(subscrybers.values.filter { (subscryber) -> Bool in
            return self.mainContext.deletedObjects.contains(where: {contains($0, subscryber)}) ||
                self.mainContext.insertedObjects.contains(where: {contains($0, subscryber)}) ||
                self.mainContext.updatedObjects.contains(where: {contains($0, subscryber)})
        })
        try mainContext.save()
        subs.forEach{$0.call()}
    }
}

extension CoreDataStack {
    enum Error: Swift.Error {
        case fetch
    }
    
    struct Subscriber {
        let type: NSManagedObject.Type
        var call: () -> ()
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

