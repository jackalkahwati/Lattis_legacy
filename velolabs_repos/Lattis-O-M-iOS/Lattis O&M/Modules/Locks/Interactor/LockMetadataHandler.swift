//
//  LockMetadataHandler.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 8/15/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Oval

final class LockMetadataHandler {
    var isMetadataReseived = false {didSet {send()}}
    var isFWVersionReseived = false {didSet {send()}}
    fileprivate var isSent = false
    fileprivate let lock: Lock
    init(lock: Lock) {
        self.lock = lock
    }
    
    private func send() {
        guard isMetadataReseived && isFWVersionReseived && isSent == false && lock.params().isEmpty == false else { return }
        isSent = true
        Session.shared.send(metadata: .ellipse(lock, false)) { _ in }
    }
}
