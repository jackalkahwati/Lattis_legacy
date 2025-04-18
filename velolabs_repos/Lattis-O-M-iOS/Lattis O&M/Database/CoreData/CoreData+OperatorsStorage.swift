//
//  CoreData+OperatorsStorage.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 5/17/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import CoreData

extension CoreDataStack: OperatorsStorage {
    func save(_ operators: [Operator], for fleet: Fleet, update: Bool) {
        write(completion: { (context) in
            do {
                let operatorIds = operators.map{$0.operatorId}
                if update {
                    let remove = try CDOperator.all(in: context, with: NSPredicate(format: "NOT (operatorId IN %@) AND SUBQUERY(fleets, $fleet, $fleet.fleetId = %@).@count > 0", operatorIds, NSNumber(value: fleet.fleetId)))
                    remove.forEach({context.delete($0)})
                }
                let existed = try CDOperator.all(in: context, with: NSPredicate(format: "operatorId IN %@", operatorIds))
                let cdFleet = try CDFleet.find(in: context, with: NSPredicate(format: "fleetId = %@", NSNumber(value: fleet.fleetId)))
                operators.forEach({ (oper) in
                    let cdOper = existed.filter({$0.operatorId == oper.operatorId}).first ?? CDOperator.create(in: context)
                    cdOper.fill(with: oper)
                    if let fleet = cdFleet {cdOper.addToFleets(fleet)}
                })
            } catch {
                print(error)
            }
        }, fail: {print($0)}){}
    }
    
    func getOperators(by fleet: Fleet) -> [Operator] {
        do {
            let operators: [CDOperator] = try read(with: NSPredicate(format: "SUBQUERY(fleets, $fleet, $fleet.fleetId = %@).@count > 0", NSNumber(value: fleet.fleetId)))
            return operators.map(Operator.init)
        } catch {
            print(error)
            return []
        }
    }
}

extension Operator {
    init(oper: CDOperator) {
        self.operatorId = Int(oper.operatorId)
        self.firstName = oper.firstName
        self.lastName = oper.lastName
        self.email = oper.email
        self.phoneNumber = oper.phoneNumber
        self.countryCode = oper.countryCode
    }
}

extension CDOperator {
    func fill(with oper: Operator) {
        self.operatorId = Int32(oper.operatorId)
        self.firstName = oper.firstName
        self.lastName = oper.lastName
        self.email = oper.email
        self.phoneNumber = oper.phoneNumber
        self.countryCode = oper.countryCode
        do {
            let assigned = try CDTicket.all(in: managedObjectContext!, with: NSPredicate(format: "assigneeId = %@", NSNumber(value: oper.operatorId)))
            self.assigned = NSSet(array: assigned)
            let owned = try CDTicket.all(in: managedObjectContext!, with: NSPredicate(format: "operatorId = %@", NSNumber(value: oper.operatorId)))
            self.owned = NSSet(array: owned)
        } catch {
            print(error)
        }
    }
}
