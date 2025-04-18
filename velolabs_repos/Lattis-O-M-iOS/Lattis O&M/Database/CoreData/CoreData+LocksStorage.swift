//
//  CoreData+LocksStorage.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 5/11/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import CoreData
import CoreLocation

extension CoreDataStack: LocksStorage {
    fileprivate var check: (String) -> Bool {
        return { string in
            return String(describing: CDEllipse.self) == string
        }
    }
    
    func lock(with macId: String) -> Ellipse? {
        do {
            let lock = try CDEllipse.find(in: mainContext, with: NSPredicate(format: "macId = %@", macId))
            return  lock == nil ? nil : Ellipse(ellipse: lock!)
        } catch {
            print(error)
        }
        return nil
    }
    
    func save(_ locks: [Ellipse], update: Bool, completion: @escaping () -> ()) {
        write(completion: { (context) in
            do {
                let lockIds = locks.map{$0.lockId}
                if update {
//                    let fleetIds = Array(Set(locks.map{$0.fleetId}))
                    let remove = try CDEllipse.all(in: context, with: NSPredicate(format: "NOT (lockId IN %@)", lockIds))
                    remove.forEach({context.delete($0)})
                }
                let existed = try CDEllipse.all(in: context, with: NSPredicate(format: "lockId IN %@", lockIds))
                locks.forEach({ (ellipse) in
                    let lock = existed.filter({$0.lockId == ellipse.lockId}).first ?? CDEllipse.create(in: context)
                    lock.fill(with: ellipse)
                })
            } catch {
                print(error)
            }
        }, fail: {print($0)}, after: completion)
    }
    
    func subscribe(in fleet: Fleet, completion: @escaping ([Ellipse]) -> ()) -> StorageHandler {
        return handler {
            self.queue.addOperation {
                DispatchQueue.main.async {
                    do {
                        let result: [CDEllipse] = try self.read(with: NSPredicate(format: "fleet.fleetId = %@", NSNumber(value: fleet.fleetId)))
                        completion(result.compactMap(Ellipse.init).sorted(by: {$0.lockId > $1.lockId}))
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

extension Ellipse {
    init?(ellipse: CDEllipse) {
        guard let macId = ellipse.macId else { return nil }
        self.macId = macId
        self.lockId = Int(ellipse.lockId)
        self.fleetId = Int(ellipse.fleetId)
        self.fleetKey = ellipse.fleetKey
        self.name = ellipse.name
        self.bikeId = ellipse.bikeId == -1 ? nil : Int(ellipse.bikeId)
        self.bikeName = ellipse.bikeName
        self.latitude = ellipse.latitude
        self.longitude = ellipse.longitude
        self.eBikeKey = nil 
        self.emptyPin = nil
    }
}

extension CDEllipse {
    func fill(with ellipse: Ellipse) {
        self.macId = ellipse.macId
        self.lockId = Int32(ellipse.lockId)
        self.fleetId = Int32(ellipse.fleetId)
        self.fleetKey = ellipse.fleetKey
        self.name = ellipse.name
        self.bikeId = Int32(ellipse.bikeId ?? -1)
        self.bikeName = ellipse.bikeName
        self.latitude = ellipse.coordinate.latitude
        self.longitude = ellipse.coordinate.longitude
        do {
            self.fleet = try CDFleet.find(in: managedObjectContext!, with: NSPredicate(format: "fleetId = %@", NSNumber(value: ellipse.fleetId)))
            let tickets = try CDTicket.all(in: managedObjectContext!, with: NSPredicate(format: "lockId = %@", NSNumber(value: ellipse.lockId)))
            self.tickets = NSSet(array: tickets)
        } catch {
            print(error)
        }
    }
}
