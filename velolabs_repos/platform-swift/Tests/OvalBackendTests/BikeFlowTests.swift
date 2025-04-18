

import XCTest
@testable import OvalBackend
import OvalModels

class BikeFlowTests: XCTestCase {
    var oval = OvalBackend("http://lattisapp-development.lattisapi.io")
    var prod = OvalBackend("https://lattisappv2.lattisapi.io")
    
    override  func setUp() {
        oval.signIn(with: "7cc84e2eed2862859e06c1cdebfe4632c7faba5d4c6f483fc14c6b07877a292aa9182ff09cf110c466867f06ed4c58b2")
        oval.userAgent = "lattis"
        
//        prod.userAgent = "ourbike"
//        prod.signIn(with: "29a8bdf080aa9c45c79f354501b08645e7d47ec235b7ef911a83efe88adeee417153f2ae810a4fa6e55563481d95d28a")
    }
    
    func testRentals() async throws {
        let status: Status = try await oval.get(.init(.rentals, path: "get-current-status"))
        XCTAssert(status.trip == nil)
    }
    
    func testCurrentStatus() async throws {
        let status: Status = try await oval.post(Status.Device(), endpoint: .init(.users, path: "get-current-status"))
        XCTAssert(status.trip == nil)
    }
    
    func testScanQRcode() async throws {
        struct JSON: Codable {
            let qrCodeId: Int
        }
        let bike: Bike = try await prod.post(JSON(qrCodeId: 10001690), endpoint: .init(.bikes, path: "get-bike-details"))
    }
    
    func testQRScan() async throws {
        let result: ScanResult = try await oval.get(.init(.rentals, path: "find", query: [.init(name: "qr_code", value: "XXXVUS")]))
        XCTAssert(result.hub != nil)
    }
    
    func testBooking() async throws {
        let booking: Booking = try await prod.post(Bike(bikeId: 3311), endpoint: .init(.bikes, path: "create-booking"))
        XCTAssert(booking.booking_id != 0)
    }
    
    func testStartTrip() async throws {
        let trip: Trip = try await prod.post(Trip.Start(bikeId: 3311, latitude: 55.838171, longitude: 49.082775), endpoint: .init(.trips, path: "start-trip"))
    }
}


extension Trip {
    struct Start: Codable {
        let bikeId: Int
        let latitude: Double
        let longitude: Double
    }
}
