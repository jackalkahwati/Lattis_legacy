//
//  TicketsStorage.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 5/16/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

protocol TicketsStorage {
    func save(_ tickets: [Ticket], for fleet: Fleet, update: Bool, completion: @escaping () -> ())
    func subscribe(in fleet: Fleet, completion: @escaping ([Ticket]) -> ()) -> StorageHandler
}
