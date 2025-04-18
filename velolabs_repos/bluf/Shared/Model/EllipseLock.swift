//
//  EllipseLock.swift
//  BLUF
//
//  Created by Ravil Khusainov on 22.08.2020.
//

import Foundation

struct FakeEllipse {
    let macId: String
}

struct EllipseLock: Identifiable {
    
    init(metadata: EllipseLock.Metadata, device: FakeEllipse? = nil) {
        self.metadata = metadata
        self.device = device
    }
    
    init(metadata: EllipseLock.Metadata) {
        self.metadata = metadata
        self.device = nil
    }
    
    let metadata: Metadata
    let device: FakeEllipse?
        
    var id: Int { metadata.id }
    var macId: String { metadata.macId.uppercased() }
    var name: String { metadata.name ?? "null" }
    
    struct Metadata: Codable {
        let id: Int
        let macId: String
        let name: String?
    }
}

#if DEBUG
extension Array where Element == EllipseLock {
    static var stabs: [Element] {
        var s: [Element] = []
        for idx in (1...100) {
            s.append(.init(metadata: .init(id: idx, macId: UUID().uuidString, name: "Fake-\(idx)"), device: nil))
        }
        return s
    }
}
#endif
