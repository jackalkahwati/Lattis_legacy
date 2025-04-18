//
//  CoreData+TicketsStorage.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 5/16/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import CoreData
import CoreLocation

extension CoreDataStack: TicketsStorage {
    fileprivate var check: (String) -> Bool {
        return { string in
            return String(describing: CDTicket.self) == string
        }
    }
    
    func save(_ tickets: [Ticket], for fleet: Fleet, update: Bool, completion: @escaping () -> ()) {
        write(completion: { (context) in
            do {
                let ticketIds = tickets.map{$0.ticketId}
                if update {
                    var fleetIds = Array(Set(tickets.map{$0.fleetId}))
                    fleetIds.append(fleet.fleetId)
                    let remove = try CDTicket.all(in: context, with: NSPredicate(format: "NOT (ticketId IN %@) AND (fleetId IN %@)", ticketIds, fleetIds))
                    remove.forEach({context.delete($0)})
                }
                let existed = try CDTicket.all(in: context, with: NSPredicate(format: "ticketId IN %@", ticketIds))
                tickets.forEach({ (ticket) in
                    let tick = existed.filter({$0.ticketId == ticket.ticketId}).first ?? CDTicket.create(in: context)
                    tick.fill(with: ticket)
                })
            } catch {
                print(error)
            }
        }, fail: {print($0)}, after: completion)
    }
    
    func subscribe(in fleet: Fleet, completion: @escaping ([Ticket]) -> ()) -> StorageHandler {
        return handler {
            self.queue.addOperation {
                DispatchQueue.main.async {
                    do {
                        let result: [CDTicket] = try self.read(with: NSPredicate(format: "fleet.fleetId = %@", NSNumber(value: fleet.fleetId)))
                        completion(result.compactMap(Ticket.init))
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

extension Ticket {
    init?(ticket: CDTicket) {
        self.ticketId = Int(ticket.ticketId)
        self.fleetId = Int(ticket.fleetId)
        self.created = (ticket.created as Date?) ?? Date()
        self.name = ticket.name
        self.categoryString = ticket.category
        self.resolved = ticket.resolved as Date?
        self.status = Status(string: ticket.status)
        self.type = TicketType(string: ticket.type)
        self.maintenanceNotes = ticket.maintenanceNotes
        self.operatorNotes = ticket.operatorNotes
        self.riderNotes = ticket.riderNotes
        self.lockId = Int(ticket.lockId)
        self.assigneeId = Int(ticket.assigneeId)
        self.operatorId = ticket.operatorId == -1 ? nil : Int(ticket.operatorId)
        self.bikeId = Int(ticket.bikeId)
        self.userPhoto = ticket.userPhoto == nil ? nil : URL(string: ticket.userPhoto!)
        self.operatorPhoto = ticket.operatorPhoto == nil ? nil : URL(string: ticket.operatorPhoto!)
        self.isNew = ticket.isNew
        
        self.bikeName = ticket.bikeName
        self.lockName = ticket.lockName
        self.bikeStatus = Bike.Status(string: ticket.bikeStatus)
        self.ticketStatus = ticket.ticketStatus
        self.currentStatus =  Bike.CurrentStatus(string: ticket.currentStatus)
        self.latitude = ticket.latitude
        self.longitude = ticket.longitude
    }
}

extension CDTicket {
    func fill(with ticket: Ticket) {
        self.ticketId = Int32(ticket.ticketId)
        self.fleetId = Int32(ticket.fleetId)
        self.created = ticket.created
        self.name = ticket.name
        self.category = ticket.categoryString
        self.resolved = ticket.resolved
        self.status = ticket.status?.rawValue
        self.type = ticket.type?.rawValue
        self.maintenanceNotes = ticket.maintenanceNotes
        self.operatorNotes = ticket.operatorNotes
        self.riderNotes = ticket.riderNotes
        self.lockId = Int32(ticket.lockId ?? -1)
        self.assigneeId = Int32(ticket.assigneeId ?? -1)
        self.operatorId = Int32(ticket.operatorId ?? -1)
        self.userPhoto = ticket.userPhoto?.absoluteString
        self.bikeId = Int32(ticket.bikeId)
        self.operatorPhoto = ticket.operatorPhoto?.absoluteString
        
        self.bikeName = ticket.bikeName
        self.lockName = ticket.lockName
        self.bikeStatus = ticket.bikeStatus?.rawValue
        self.ticketStatus = ticket.ticketStatus
        self.currentStatus = ticket.currentStatus?.rawValue
        self.latitude = ticket.coordinate.latitude
        self.longitude = ticket.coordinate.longitude
        
        if let isNew = ticket.isNew {
            self.isNew = isNew
        }
        do {
            self.fleet = try CDFleet.find(in: managedObjectContext!, with: NSPredicate(format: "fleetId = %@", NSNumber(value: ticket.fleetId)))
            if let lockId = ticket.lockId {
                self.lock = try CDEllipse.find(in: managedObjectContext!, with: NSPredicate(format: "lockId = %@", NSNumber(value: lockId)))
            }
            self.assignee = try CDOperator.find(in: managedObjectContext!, with: NSPredicate(format: "operatorId = %@", NSNumber(value: self.assigneeId)))
            if let operId = ticket.operatorId {
                self.oper = try CDOperator.find(in: managedObjectContext!, with: NSPredicate(format: "operatorId = %@", NSNumber(value: operId)))
            }
        } catch {
            print(error)
        }
    }
}
