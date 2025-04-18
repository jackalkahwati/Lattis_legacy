//
//  OvalTests.swift
//  OvalTests
//
//  Created by Ravil Khusainov on 7/11/18.
//  Copyright © 2018 Lattis inc. All rights reserved.
//

import XCTest
import Oval

class OvalTests: XCTestCase {
    let session = Session.shared
    
    override func setUp() {
        super.setUp()
        // Put setup code here. This method is called before the invocation of each test method in the class.
    }
    
    func testLogin() {
        struct Result: Decodable {
            let user_id: Int
            let verified: Bool
        }
        struct User: Encodable {
            let users_id: String
            let reg_id: String
            let user_type: String
            let password: String
            let is_signing_up: Bool
            let email: String
        }
        let user = User(users_id: "ravil@lattis.io", reg_id: "", user_type: "lattis", password: "ravillattis", is_signing_up: false, email: "ravil@lattis.io")
        let expect = XCTestExpectation(description: "Logging in...")
        session.send(.post(json: user, api: .lattisLogin), success: { (result: Result) in
            XCTAssertTrue(result.verified)
            expect.fulfill()
        }, fail: { error in
            XCTAssert(false)
            expect.fulfill()
        })
        wait(for: [expect], timeout: 10)
    }
    
    
    override func tearDown() {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
        super.tearDown()
    }
    
}

extension API {
    static let lattisLogin = API(path: "users/registration")
}
