//
//  UserStrorage.swift
//  Lattis
//
//  Created by Ravil Khusainov on 21/04/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation

protocol UserStorage {
    func save(_ user: User)
    func user(with userId: Int?) -> User?
    func privateNetwork(by fleetId: Int) -> PrivateNetwork?
    func shouldAddPhoneNumber(for bike: Bike) -> Bool
}
