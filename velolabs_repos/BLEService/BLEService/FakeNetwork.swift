//
//  FakeNetwork.swift
//  BLEService
//
//  Created by Ravil Khusainov on 5/27/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

class FakeNetwork: Network {
    let userId: String
    let signedMessage: String
    let publicKey: String
    
    init(userId: String, signedMessage: String, publicKey: String) {
        self.userId = userId
        self.signedMessage = signedMessage
        self.publicKey = publicKey
    }
    
    func sign(lockWith macId: String, success: @escaping (String, String, String) -> (), fail: @escaping (Error) -> ()) {
        success(signedMessage, publicKey, userId)
    }
}
