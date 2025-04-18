//
//  FakeEllipse.swift
//  EllipseLockUI
//
//  Created by Ravil Khusainov on 01.03.2020.
//

import Foundation
import EllipseLock

fileprivate var handlersStore = NSHashTable<EllipseHandler<FakeEllipse>>.weakObjects()
fileprivate var handlers: [EllipseHandler<FakeEllipse>] { handlersStore.allObjects }

final class FakeEllipse: EllipseProtocol {
    var macId: String = UUID().uuidString
    var name: String
    var connection: EllipseBLE.Connection = .disconnected
    var security: EllipseBLE.SecurityValue = .unlocked
    
    init(_ name: String) {
        self.name = name
    }
    
    static var all: [FakeEllipse] {[
        .init("Ellipse 1"),
        .init("Ellipse 2"),
        .init("Ellipse 3")
        ]}
    
    static func scan(with: EllipseHandler<FakeEllipse>) {
        add(handler: with)
    }
    
    static func add(handler: EllipseHandler<FakeEllipse>) {
        guard !handlers.contains(handler) else { return }
        handlersStore.add(handler)
    }
    
    func connect(with handler: EllipseHandler<FakeEllipse>?) {
        if let h = handler {
            h.add(self)
            FakeEllipse.add(handler: h)
        }

        self.connection = .connected
        handler?.connectionUpdated(self)
        DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
            self.connection = .paired
            handler?.connectionUpdated(self)
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 5) {
            self.security = .locked
            handlers.run(\.securityUpdated, with: self)
        }
    }
    
    func disconnect() {
        self.connection = .disconnected
        handlers.run(\.connectionUpdated, with: self)
    }
}

