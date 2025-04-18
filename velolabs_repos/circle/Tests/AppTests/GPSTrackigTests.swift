//
//  GPSTrackigTests.swift
//  
//
//  Created by Ravil Khusainov on 28.09.2021.
//

import XCTVapor
@testable import App

final class GPSTrackingTests: XCTestCase {
    
    var app: Application!
    
    override func setUp() {
        app = Application(.testing)
        do {
            try configure(app)
        } catch {
            XCTAssertThrowsError(error)
        }
    }
    
    override func tearDown() {
        app.shutdown()
    }
    
    func testSendVehicleToLive() throws {
        let expectation = XCTestExpectation(description: "Calling Send Live on GPSTracking")
        
        try GPSTracking.update(status: .live, vehicles: [1056], client: app.client)
            .whenComplete { result in
                switch result {
                case .success(let status):
                    XCTAssertEqual(status, .ok)
                    expectation.fulfill()
                case .failure(let error):
                    XCTAssertThrowsError(error)
                }
            }
        
        wait(for: [expectation], timeout: 6)
    }
}
