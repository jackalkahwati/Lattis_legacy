

import XCTest
@testable import OvalBackend

class PortFlowTests: XCTestCase {
    var oval = OvalBackend("http://lattisapp-development.lattisapi.io")
    
    override  func setUp() {
        oval.signIn(with: "7cc84e2eed2862859e06c1cdebfe4632c7faba5d4c6f483fc14c6b07877a292aa9182ff09cf110c466867f06ed4c58b2")
        oval.userAgent = "lattis"
    }
    
    func testCurrentStatus() async throws {
        let status: Status = try await oval.post(Status.Device(), endpoint: .init(.users, path: "get-current-status"))
        XCTAssert(status.trip == nil)
    }
    
    func testCreateBooking() async throws {
        let booking: Booking = try await oval.post(Port(port_id: 110), endpoint: .init(version: .v2, .bookings, path: "port"))
    }
    
    func testCancelBooking() async throws {
        let booking: Booking = try await oval.patch(Port(port_id: 110), endpoint: .init(version: .v2, .bookings, path: "9575/cancel"))
    }
    
    func testQRScan() async throws {
        let result: ScanResult = try await oval.get(.init(.rentals, path: "find", query: [.init(name: "qr_code", value: "port74-test")]))
        XCTAssert(result.hub != nil)
    }
//    func testCancelBooking() async throws {
//        let booking: Booking = try await oval.patch(Bike(bikeId: 913), endpoint: .init(version: .v2, .bookings, path: "9453/cancel"))
//    }
    
    func testUnlockKisi() async throws {
        let _: EmptyJSON! = try await oval.post(EmptyJSON(), endpoint: .init(.hubs, path: "custom/port_94cc3e7d-f308-4e2c-b6a7-6052d53a7045/unlock"))
    }
}

struct Bike: Codable {
    let bikeId: Int
}
