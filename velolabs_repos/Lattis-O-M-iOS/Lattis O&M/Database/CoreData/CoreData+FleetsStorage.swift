//
//  CoreData+FleetsStorage.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 5/14/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import CoreData

extension CoreDataStack: FleetsStorage {
    fileprivate var check: (String) -> Bool {
        return { string in
            return String(describing: CDFleet.self) == string
        }
    }
    
    var currentFleet: Fleet? {
        guard mainContext != nil else { return nil }
        do {
            guard let fleet = try CDFleet.find(in: mainContext, with: NSPredicate(format: "isCurrent = %@", NSNumber(value: true))) else { return nil }
            return Fleet(fleet: fleet)
        } catch {
            print(error)
            return nil
        }
    }
    
    func fleet(with fleetId: Int32) -> Fleet? {
        do {
            let fleet = try CDFleet.find(in: mainContext, with: NSPredicate(format: "fleetId = %@", NSNumber(value: fleetId)))
            return fleet.map(Fleet.init)
        } catch {
            print(error)
        }
        return nil
    }
    
    func save(_ fleets: [Fleet], update: Bool, completion: @escaping () -> ()) {
        write(completion: { (context) in
            do {
                let fleetIds = fleets.map{$0.fleetId}
                if update {
                    let remove = try CDFleet.all(in: context, with: NSPredicate(format: "NOT (fleetId IN %@)", fleetIds))
                    remove.forEach({context.delete($0)})
                }
                let existed = try CDFleet.all(in: context, with: NSPredicate(format: "fleetId IN %@", fleetIds))
                fleets.forEach({ (fleet) in
                    let cdFleet = existed.filter({$0.fleetId == fleet.fleetId}).first ?? CDFleet.create(in: context)
                    cdFleet.fill(with: fleet)
                    if fleets.count == 1 {
                        cdFleet.isCurrent = true
                    }
                })
            } catch {
                print(error)
            }
        }, fail: {print($0)}, after: completion)
    }
    
    func subscribe(completion: @escaping ([Fleet]) -> ()) -> StorageHandler {
        return handler {
            self.queue.addOperation {
                DispatchQueue.main.async {
                    do {
                        let result: [CDFleet] = try self.read()
                        completion(result.map(Fleet.init).sorted(by: {$0.fleetId > $1.fleetId}))
                    } catch {
                        print(error)
                        completion([])
                    }
                }
            }
        }
    }
}

private extension CoreDataStack {
    func handler(for callback: @escaping () -> ()) -> StorageHandler {
        let handler = StorageHandler(check: check, callback: callback)
        subscribe(handler: handler)
        return handler
    }
}

extension Fleet {
    init(fleet: CDFleet) {
        self.fleetId = Int(fleet.fleetId)
        self.name = fleet.name
        self.isCurrent = fleet.isCurrent
    }
}

extension CDFleet {
    func fill(with fleet: Fleet) {
        self.fleetId = Int32(fleet.fleetId)
        self.name = fleet.name
        if let isCurrent = fleet.isCurrent {
            self.isCurrent = isCurrent
        }
        do {
            let locks = try CDEllipse.all(in: managedObjectContext!, with: NSPredicate(format: "fleetId = %@", NSNumber(value: fleet.fleetId)))
            self.locks = NSSet(array: locks)
            
            let tickets = try CDTicket.all(in: managedObjectContext!, with: NSPredicate(format: "fleetId = %@", NSNumber(value: fleet.fleetId)))
            self.tickets = NSSet(array: tickets)
        } catch {
            print(error)
        }
    }
}
