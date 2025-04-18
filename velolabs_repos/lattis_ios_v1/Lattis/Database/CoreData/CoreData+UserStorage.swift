//
//  CoreData+UserStorage.swift
//  Lattis
//
//  Created by Ravil Khusainov on 21/04/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import CoreData

extension CoreDataStack: UserStorage {
    func save(_ user: User) {
        write(completion: { (context) in
            do {
                if let cdUser = try CDUser.find(in: context, with: NSPredicate(format: "userId = %@", NSNumber(value: user.userId))) {
                    cdUser.fill(with: user)
                } else {
                    let cdUser = CDUser.create(in: context)
                    cdUser.fill(with: user)
                }
            } catch {
                print(error)
            }
        }, fail: { error in
            print(error)
        })
    }
    
    func user(with userId: Int?) -> User? {
        let predicate = userId == nil ? NSPredicate(format: "isCurrent = %@", NSNumber(value: true)) : NSPredicate(format: "userId = %@", NSNumber(value: userId!))
        do {
            if let user: CDUser = try read(with: predicate).first {
                return User(cdUser: user)
            }
            return nil
        } catch  {
            print(error)
            return nil
        }
    }
    
    func privateNetwork(by fleetId: Int) -> PrivateNetwork? {
        return user(with: nil)?.privateNetworks.filter({$0.fleetId == fleetId}).first
    }
    
    func shouldAddPhoneNumber(for bike: Bike) -> Bool {
        guard let phone = self.user(with: nil)?.phoneNumber else { return bike.requirePhoneNumber }
        return bike.requirePhoneNumber && phone.isEmpty
    }
}

private extension CDUser {
    func fill(with user: User) {
        self.userId = Int32(user.userId)
        self.email = user.email
        self.firstName = user.firstName
        self.lastName = user.lastName
        self.isCurrent = user.isCurrent
        self.phoneNumber = user.phoneNumber
        self.privateNetworksArray = user.privateNetworks.compactMap { CDPrivateNetwork.create(in: self.managedObjectContext!, with: $0) }
    }
}

private extension User {
    init(cdUser: CDUser) {
        self.userId = Int(cdUser.userId)
        self.email = cdUser.email!
        self.firstName = cdUser.firstName
        self.lastName = cdUser.lastName
        self.isCurrent = cdUser.isCurrent
        self.phoneNumber = cdUser.phoneNumber
        self.privateNetworks = cdUser.privateNetworksArray.map(PrivateNetwork.init)
    }
}

private extension CDPrivateNetwork {
    func fill(with network: PrivateNetwork) {
        self.email = network.email
        self.fleetId = Int32(network.fleetId)
        self.fleetUserId = Int32(network.fleetUserId)
        self.fleetName = network.fleetName
        self.logo = network.logo?.absoluteString
        self.customerName = network.customerName
    }
    
    class func create(in context: NSManagedObjectContext, with network: PrivateNetwork) -> CDPrivateNetwork? {
        do {
            var object = try CDPrivateNetwork.find(in: context, with: NSPredicate(format: "fleetId = %@", NSNumber(value: network.fleetId)))
            if object == nil {
                object = CDPrivateNetwork.create(in: context)
            }
            object?.fill(with: network)
            return object
        } catch {
            print(error)
        }
        return nil
    }
}

private extension PrivateNetwork {
    init(cdNetwork: CDPrivateNetwork) {
        self.email = cdNetwork.email!
        self.fleetUserId = Int(cdNetwork.fleetUserId)
        self.fleetName = cdNetwork.fleetName
        self.fleetId = Int(cdNetwork.fleetId)
        self.logo = cdNetwork.logo != nil ? URL(string: cdNetwork.logo!) : nil
        self.customerName = cdNetwork.customerName
    }
}
