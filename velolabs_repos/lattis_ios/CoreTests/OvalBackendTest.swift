//
//  OvalBackendTest.swift
//  CoreTests
//
//  Created by Ravil Khusainov on 15.03.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import XCTest
@testable import LattisCore
@testable import OvalBackend
@testable import Model

final class OvalBackendTest: XCTestCase {
    
    let oval = OvalBackend("https://lattisappv2.lattisapi.io")
    
    override func setUp() {
        oval.update(header: .userAgent, with: "sandy-pedals")
        oval.signIn(with: "f63d73d6749734f58222a6e931ea5b78b1f61196bf26434dec00c7c882b4cca17a6387abf519a8945fdbd9645aa65ca9")
    }
//    
//    func testQrCodeScan() async throws {
//        struct JSON: Codable {
//            let qrCodeId: Int
//        }
//        let bike: Bike = try await oval.post(JSON(qrCodeId: 10001690), endpoint: .init(.bikes, path: "get-bike-details"))
//    }
    
    func testGetIoTStatus() async throws {
        let status: Thing.Status! = try await oval.get(.init(.bikes, path: "\(1610)/iot/status", query: [.init(name: "controller_key", value: "867604059823378")]))
        print(status)
    }
}
