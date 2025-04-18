//
//  CircleAPI.swift
//  Clip Lattis
//
//  Created by Ravil Khusainov on 26.01.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Foundation

enum CircleAPI {
    
    static fileprivate(set) var agent = NetworkAgent(Env.circleURL)
    
    static func logOut() {
        agent.authorization = nil
    }
    
    static func logIn(_ token: String) {
        agent.authorization = .bearer(token)
    }
}

extension CircleAPI {
    static func signIn(_ user: OAuthUser) async throws -> User {
        try await agent.run(.post(user, path: "clip/signin"))
    }
}
