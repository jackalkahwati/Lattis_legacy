//
//  Ellipse.swift
//  Lattis
//
//  Created by Ravil Khusainov on 18/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation

public struct Ellipse {
    public var macId: String?
    public var lockId: Int32
    public var userId: Int32
    public var usersId: String?
    public var name: String?
    public var sharedToUserId: Int32
    public var shareId: Int32
}

public extension Ellipse {
    init(macId: String) {
        self.macId = macId
        lockId = 0
        userId = 0
        usersId = nil
        name = nil
        sharedToUserId = 0
        shareId = 0
    }
}
