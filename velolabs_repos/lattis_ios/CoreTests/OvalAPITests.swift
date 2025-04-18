//
//  OvalAPITests.swift
//  CoreTests
//
//  Created by Ravil Khusainov on 21.01.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import XCTest
@testable import LattisCore
import OvalAPI
import Model

class OvalAPITests: XCTestCase {
    
    var api: (UserAPI & AppsAPI & BikeAPI & HubsAPI)!

    override func setUp() {
        signIn(.jeremy)
        api = Session.shared
    }
    
    fileprivate func signIn(_ user: TestUser) {
        Session.userAgent = user.userAgent
        Session.shared.storage.userId = user.id
        Session.shared.storage.restToken = user.token
    }

    override func tearDown() {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }

    func testGetUser() throws {
        
        let expectation = XCTestExpectation(description: "Getting user info")
        
        api.refresh { result in
            switch result {
            case .failure(let error):
                print(error)
                XCTAssert(false)
            case .success:
                XCTAssert(true)
            }
            expectation.fulfill()
        }
        wait(for: [expectation], timeout: 3)
    }
    
    func testGetAppInfo() throws {
        let expectation = XCTestExpectation(description: "Getting app info")
        
        api.fetchInfo { result in
            switch result {
            case .failure(let error):
                print(error)
                XCTAssert(false)
            case .success:
                XCTAssert(true)
            }
            expectation.fulfill()
        }
        
        wait(for: [expectation], timeout: 3)
    }
    
    func testScanQRCode() throws {
        let expectation = XCTestExpectation(description: "Getting app info")
        
        api.getBike(by: nil, qrCodeId: 10001690, iotCode: nil, completion: { result in
            switch result {
            case .failure(let error):
                print(error)
                XCTAssert(false)
            case .success(let bike):
                print(bike)
                XCTAssert(true)
            }
            expectation.fulfill()
        })
        
        wait(for: [expectation], timeout: 3)
    }
    
    func testFetchRentals() throws {
        let expectation = XCTestExpectation(description: "Getting app info")
        api.fetchRentals(in: .init(ne: .init(55.850082,49.099571), sw: .init(55.827591,49.067864))) { result in
            switch result {
            case .failure(let error):
                print(error)
                XCTAssert(false)
            case .success(let bike):
                print(bike)
                XCTAssert(true)
            }
            expectation.fulfill()
        }
        wait(for: [expectation], timeout: 3)
    }
    
    func testNewQrCodeFlow() async throws {
        let rental = try await api.find(by: "really-uniq")
        print(rental)
    }
}

private struct TestUser: Codable {
    let id: Int
    let token: String
    let userAgent: String
    
    static let alvin = TestUser(id: 12690, token: "c79c8bcb22c5f8fba61ba82aea9ac9bbb76c7ddfff29a0876a9b7722a5fec831629f2c3841d58c2f10b3fd8cdafc228c", userAgent: "lattis")
    static let ravil = TestUser(id: 908, token: "7cc84e2eed2862859e06c1cdebfe4632c7faba5d4c6f483fc14c6b07877a292aa9182ff09cf110c466867f06ed4c58b2", userAgent: "lattis")
//    static let jeremy = TestUser(id: 970, token: "f63d73d6749734f58222a6e931ea5b78cc709389977e2808e07c4e66f4d192a01b17df6d6427aad6b098def6029d0772", userAgent: "lattis")
    static let sergio = TestUser(id: 20060, token: "89806a1663d9aa9e55796479a001e144066c28046577d094b83dc9ff2c91a56a7f729134d559b948310dd41b8401ab7a22b0dd6a6d9da7ab7b07773453d33ddc", userAgent: "grin")
    static let jeremy = TestUser(id: 5195, token: "f63d73d6749734f58222a6e931ea5b78b1f61196bf26434dec00c7c882b4cca17a6387abf519a8945fdbd9645aa65ca9", userAgent: "ourbike")
}
