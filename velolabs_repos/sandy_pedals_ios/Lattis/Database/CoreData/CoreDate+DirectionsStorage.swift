//
//  CoreDate+DirectionsStorage.swift
//  Lattis
//
//  Created by Ravil Khusainov on 22/04/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import CoreLocation

extension CoreDataStack: DirectionsStorage {
    var recient: [Direction] {
        do {
            let descriptors: [NSSortDescriptor] = [NSSortDescriptor(key: "rating", ascending: false), NSSortDescriptor(key: "name", ascending: true)]
            let all = try CDDirection.all(in: mainContext, sortetBy: descriptors).map(Direction.init)
            return all
        } catch {
            print(error)
            return []
        }
    }
    
    func save(_ direction: Direction) {
        guard let name = direction.name else { return }
        write(completion: { (context) in
            do {
                let all = try CDDirection.all(in: context, sortetBy: [NSSortDescriptor(key: "rating", ascending: false)])
                let rating: Int32 = all.first?.rating ?? 0
                if let dir = try CDDirection.find(in: context, with: NSPredicate(format: "name = %@", name)) {
                    dir.fill(with: direction)
                    dir.rating = rating + 1
                } else {
                    let dir = CDDirection.create(in: context)
                    dir.createdAt = Date()
                    dir.fill(with: direction)
                    dir.rating = rating + 1
                }
            } catch {
                print(error)
            }
        }, fail: { error in
            print(error)
        })
    }
}

extension CDDirection {
    func fill(with direction: Direction) {
        self.name = direction.name
        self.latitude = direction.coordinate.latitude
        self.longitude = direction.coordinate.longitude
        self.address = direction.address
        self.rating = direction.rating
    }
}

extension Direction {
    init(direction: CDDirection) {
        self.name = direction.name
        self.address = direction.address
        self.coordinate = CLLocationCoordinate2D(latitude: direction.latitude, longitude: direction.longitude)
        self.rating = direction.rating
        self.placeId = nil
    }
}
