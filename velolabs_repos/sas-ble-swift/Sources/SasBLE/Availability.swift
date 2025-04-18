//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 11.04.2022.
//

import Foundation
import Combine

final class Availability {
    let report: PassthroughSubject<[UUID], Never> = .init()
    var timer: Timer!
    var identifiers: Set<UUID> = []
    var checklist: Set<UUID> = []
    
    init(_ duration: TimeInterval = 10) {
        timer = .scheduledTimer(withTimeInterval: duration, repeats: true, block: { [weak self] timer in
            self?.check()
        })
    }
    
    deinit {
        timer.invalidate()
    }
    
    func check() {
        let diff = checklist.subtracting(identifiers)
        if !diff.isEmpty {
            report.send(Array(diff))
        }
        checklist.subtract(diff)
        identifiers.removeAll()
    }
}
