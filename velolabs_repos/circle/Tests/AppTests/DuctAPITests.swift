//
//  DuctAPITests.swift
//  
//
//  Created by Ravil Khusainov on 06.10.2021.
//
import XCTVapor
@testable import App

final class DuctAPITests: XCTestCase {
    
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
    
    func testCheckStatus() async throws {
        let thing = Thing()
        thing.id = 949
        thing.fleetId = 73
        thing.key = "s101026"
        thing.$bike.id = 2859
        
        let req = Request(application: app, method: .GET, url: URI(string: "https://api.lattis.io"), on: app.eventLoopGroup.next())
        let status = try await DucktAPI.status(of: thing, req: req)
        print(status)
        XCTAssert(true)
    }
    
    func testCheckThingStatus() async throws {
        let thing = Thing()
        thing.id = 949
        thing.fleetId = 73
        thing.key = "s101026"
        thing.$bike.id = 2859
        
        let req = Request(application: app, method: .GET, url: URI(string: "https://api.lattis.io/operator/things/949/status"), on: app.eventLoopGroup.next())
        
        let controller = ThingController()
        
        let status = try await controller.status(req: req)
            .get()
        print(status)
        XCTAssert(true)
    }
}
