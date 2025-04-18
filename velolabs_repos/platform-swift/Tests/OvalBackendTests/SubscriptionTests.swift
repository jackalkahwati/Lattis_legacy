//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 11.04.2022.
//

import XCTest
@testable import OvalBackend

final class SubscriptionTests: XCTestCase {
    
    var oval = OvalBackend("http://lattisapp-development.lattisapi.io")
    var prod = OvalBackend("https://lattisappv2.lattisapi.io")
    
    override  func setUp() {
        oval.signIn(with: "7cc84e2eed2862859e06c1cdebfe4632c7faba5d4c6f483fc14c6b07877a292aa9182ff09cf110c466867f06ed4c58b2")
        oval.userAgent = "lattis"
        
        prod.userAgent = "velo_transit"
        prod.signIn(with: "f63d73d6749734f58222a6e931ea5b78b1f61196bf26434dec00c7c882b4cca17a6387abf519a8945fdbd9645aa65ca9")
    }
    
    func testFetchSubscriptions() async throws {
        struct Sub: Codable {
            let membership_subscription_id: Int
        }
        let subs: [Sub] = try await prod.get(.init(.memberships))
    }
}
