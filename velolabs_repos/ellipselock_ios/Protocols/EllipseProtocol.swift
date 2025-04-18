//
//  Ellipse.swift
//  EllipseLock
//
//  Created by Ravil Khusainov on 28.02.2020.
//

import Foundation

public protocol EllipseProtocol: Identifiable, Equatable {
    var macId: String { get }
    var name: String { get }
    var connection: EllipseBLE.Connection { get }
    var security: EllipseBLE.SecurityValue { get }
    
    func connect(with handler: EllipseHandler<Self>?)
    func disconnect()
    
    static var all: [Self] { get }
    static func scan(with: EllipseHandler<Self>)
    static func add(handler: EllipseHandler<Self>)
}

public extension EllipseProtocol {
    var id: String { macId }
    static func ==(lhs: Self, rhs: Self) -> Bool {
        lhs.macId == rhs.macId
    }
}
