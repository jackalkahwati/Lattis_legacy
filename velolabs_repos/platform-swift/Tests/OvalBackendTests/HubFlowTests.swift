
import XCTest
@testable import OvalBackend

class HubFlowTests: XCTestCase {
    
    var oval = OvalBackend("http://lattisapp-development.lattisapi.io")
    
    override  func setUp() {
        oval.signIn(with: "7cc84e2eed2862859e06c1cdebfe4632c7faba5d4c6f483fc14c6b07877a292aa9182ff09cf110c466867f06ed4c58b2") // Ravil
//        oval.signIn(with: "b2df9838958a2284f6a46e246bfd82e0258712cc724210a5f77ffad27bad6f702f8aca1f3a714298357325bcae5b1818") // Jignesh
        oval.userAgent = "lattis"
    }
    
    func testCurrentStatus() async throws {
        let status: Status = try await oval.post(Status.Device(), endpoint: .init(.users, path: "get-current-status"))
        XCTAssert(status.trip == nil)
    }
    
    func testGetTripDetails() async throws {
        let trip: Trip = try await oval.post(Trip(trip_id: 8657), endpoint: .init(.trips, path: "get-trip-details"))
    }
    
    func testQRScan() async throws {
        let result: ScanResult = try await oval.get(.init(.rentals, path: "find", query: [.init(name: "qr_code", value: "really-uniq")]))
        XCTAssert(result.hub != nil)
    }
    
    func testBooking() async throws {
        let booking: Booking = try await oval.post(Hub(hub_id: 51), endpoint: .init(version: .v2, .bookings, path: "hub"))
        XCTAssert(booking.booking_id != 0)
    }
    
    func testStartingTrip() async throws {
        oval.update(\.dateRpresentation, value: .secondsSince1970)
        let trip: Trip = try await oval.post(Hub(hub_id: 51), endpoint: .init(.trips, path: "start-trip"))
        XCTAssert(false)
    }
    
    func testTripDetails() async throws {
        let trip: Trip = try await oval.post(Trip(trip_id: 8559), endpoint: .init(.trips, path: "get-trip-details"))
    }
    
    func testUpdateTrip() async throws {
        let update: Trip.Invoice = try await oval.post(Trip.Update(trip_id: 8530, steps: []), endpoint: .init(.trips, path: "update-trip"))
    }
    
    func testEndTrip() async throws {
        let trip: Trip = try await oval.post(Trip.End(trip_id: 8670, latitude: 55.838171, longitude: 49.082775), endpoint: .init(.trips, path: "end-trip"))
    }
    
    func testCancelBooking() async throws {
        let booking: Booking = try await oval.patch(Hub(hub_id: 51), endpoint: .init(version: .v2, .bookings, path: "9471/cancel"))
    }
}

struct AppInfo: Codable {
    let email: String
}

struct Hub: Codable {
    let hub_id: Int
}

struct Port: Codable {
    let port_id: Int
}

struct ScanResult: Codable {
    let hub: Hub?
}

struct Booking: Codable {
    let booking_id: Int
}

struct Status: Codable {
    let trip: Trip?
    let reservation: Int?
    let active_booking: Int?
    let support_phone: String
    let on_call_operator: String?
    
    struct Trip: Codable {
        let trip_id: Int
    }
    
    struct Device: Encodable {
        let device_model: String = "iPhone 12 mini"
        let device_os: String = "iOS 15.3"
    }
}

struct Trip: Codable {
    let trip_id: Int
}

extension Trip {
    struct End: Codable {
        let trip_id: Int
        let latitude: Double
        let longitude: Double
    }
    
    struct Update: Codable {
        let trip_id: Int
        let steps: [Int]
    }
    
    struct Invoice: Codable {
        
    }
}
