//
//  CoreData+TripStorage.swift
//  Lattis
//
//  Created by Ravil Khusainov on 15/03/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import CoreData
import CoreLocation

extension CoreDataStack: TripsStorage {
    func subsribe(target: AnyHashable, callback: @escaping ([Trip]) -> ()) {
        subscribe(target: target, completion: { (trips: [CDTrip]) in
            callback(trips.map(Trip.init))
        })
    }

    func update(trips: [Trip]) {
        write(completion: { (context) in
            do {
                let tripIds = trips.map{$0.tripId}
                let remove = try CDTrip.all(in: context, with: NSPredicate(format: "NOT (tripId IN %@)", tripIds))
                remove.forEach({context.delete($0)})
                let existed = try CDTrip.all(in: context, with: NSPredicate(format: "tripId IN %@", tripIds))
                trips.forEach({ (trip) in
                    let lock = existed.filter({$0.tripId == trip.tripId}).first ?? CDTrip.create(in: context)
                    lock.fill(with: trip)
                })
            } catch {
                print(error)
            }
        }, fail: {print($0)}, after: {})
    }
    
    func save(_ trip: Trip) {
        write(completion: { (context) in
            do {
                var trp = try CDTrip.find(in: context, with: NSPredicate(format: "tripId = %@", NSNumber(value: trip.tripId)))
                if trp == nil {
                    trp = CDTrip.create(in: context)
                }
                trp?.fill(with: trip)
            } catch {
                print(error)
            }
            
        }, fail: { error in
            print(error)
        })
    }
    
    func trip(by id: Int) -> Trip? {
        guard let trp = try? CDTrip.find(in: mainContext, with: NSPredicate(format: "tripId = %@", NSNumber(value: id))) else { return nil }
        return Trip(trp)
    }
}

extension CDTrip {
    func fill(with trip: Trip) {
        self.canSaveSteps = trip.canSaveSteps
        self.tripId = Int32(trip.tripId)
        self.steps?.forEach({self.managedObjectContext?.delete($0 as! NSManagedObject)})
        self.startedAt = trip.startedAt
        self.finishedAt = trip.finishedAt
        self.duration = trip.duration
        self.endAddress = trip.endAddress
        self.startAddress = trip.startAddress
        self.deposit = trip.deposit ?? -1
        self.distance = trip.distance ?? -1
        self.total = trip.total ?? -1
        self.price = trip.price ?? -1
        self.penaltyFees = trip.penaltyFees ?? -1
        self.fleetType = trip.fleetType.rawValue
        self.fleetName = trip.fleetName
        self.refundCriteriaUnit = trip.refundCriteriaUnit
        self.refundCriteria = Int32(trip.refundCriteria ?? -1)
        trip.steps.forEach { (step) in
            let stp = CDTripStep.create(in: self.managedObjectContext!)
            stp.fill(with: step)
            self.addToSteps(stp)
        }
        self.creditCardType = trip.card?.cardType?.rawValue
        self.creditCardNumber = trip.card?.number
        self.isCancelled = trip.isCanceled
        self.isStarted = trip.isStarted
        self.currency = trip.currency
    }
}

extension Trip {
    init(_ trip: CDTrip) {
        self.canSaveSteps = trip.canSaveSteps
        self.tripId = Int(trip.tripId)
        self.bikeId = Int(trip.bikeId)
        self.deposit = trip.deposit.nulyfy
        self.distance = trip.distance.nulyfy
        self.total = trip.total.nulyfy
        self.price = trip.price.nulyfy
        self.penaltyFees = trip.penaltyFees.nulyfy
        self.startedAt = trip.startedAt as Date?
        self.finishedAt = trip.finishedAt as Date?
        self.serverDuration = trip.duration
        self.endAddress = trip.endAddress
        self.startAddress = trip.startAddress
        self.steps = trip.steps?.allObjects.compactMap({$0 as? CDTripStep}).map(Step.init) ?? []
        self.fleetName = trip.fleetName
        self.refundCriteriaUnit = trip.refundCriteriaUnit
        self.refundCriteria = Int(trip.refundCriteria).nulyfy
        if let sType = trip.fleetType, let type = Bike.FleetType(rawValue: sType) {
            self.fleetType = type
        } else {
            self.fleetType = .privateFree
        }
        if let number = trip.creditCardNumber {
            self.card = CreditCard(number: number, typeString: trip.creditCardType)
        }
        self.isCanceled = trip.isCancelled
        self.isStarted = trip.isStarted
        self.currency = trip.currency ?? "USD"
    }
}

extension CDTripStep {
    func fill(with step: Trip.Step) {
        self.latitude = step.location.latitude
        self.longitude = step.location.longitude
        self.time = step.time
    }
}

extension Trip.Step {
    init(_ step: CDTripStep) {
        self.location = CLLocationCoordinate2D(latitude: step.latitude, longitude: step.longitude)
        self.time = (step.time as Date?) ?? Date()
        if step.lockState < 0 {
            self.lockState = .none
        } else if step.lockState == 0 {
            self.lockState = .track(false)
        } else {
            self.lockState = .track(true)
        }
    }
}

private extension Double {
    var nulyfy: Double? {
        return self < 0 ? nil : self
    }
}

private extension Int {
    var nulyfy: Int? {
        return self < 0 ? nil : self
    }
}
