//
//  StatisticsAuthenticator.swift
//  
//
//  Created by Ravil Khusainov on 22.05.2021.
//

import Vapor

struct StatisticsAuthenticator: RequestAuthenticator {
    func authenticate(request: Request) -> EventLoopFuture<Void> {
        do {
            let identity = try request.query.get(String.self, at: "identity")
            guard identity == "701AE02C-ACCF-4B0C-B433-43D0945782AC" else { throw Abort(.unauthorized) }
            return request.eventLoop.makeSucceededFuture(())
        } catch {
            return request.eventLoop.makeFailedFuture(error)
        }
    }
}
