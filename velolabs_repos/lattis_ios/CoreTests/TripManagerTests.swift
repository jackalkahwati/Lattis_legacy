//
//  TripManagerTests.swift
//  CoreTests
//
//  Created by Ravil Khusainov on 02.10.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import XCTest
@testable import LattisCore
import Model

class TripManagerTests: XCTestCase {
    typealias Bike = LattisCore.Bike
    typealias ModernBike = Model.Bike
    
    fileprivate var tripManager: TripManager?
    fileprivate var deviceManager: DeviceManager = .ellipseWithIot
    fileprivate let unlock = XCTestExpectation(description: "Unlock")
    fileprivate let lock = XCTestExpectation(description: "Lock")
    
    override func setUpWithError() throws {
        let semaphore = DispatchSemaphore(value: 0)
        unlock.expectedFulfillmentCount = 10
        lock.expectedFulfillmentCount = 10
        deviceManager.connect {
            TripManager.startTrip(Bike.Booking.mock(), api: API(), deviceManager: self.deviceManager, completion: { (result) in
                switch result {
                case .failure(let error):
                    XCTFail(error.localizedDescription)
                case .success(let manager):
                    self.tripManager = manager
                    NotificationCenter.default.addObserver(self, selector: #selector(self.handle(notification:)), name: .deviceStatusUpdated, object: nil)
                }
                semaphore.signal()
            })
        }
        semaphore.wait()
    }

    override func tearDownWithError() throws {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }

    func testStartTripWithQRcode() throws {
        let exp = expectation(description: "Start trip with QR-code")
        _ = TripManager.startTrip(.mock(), api: API(), deviceManager: deviceManager) { (result) in
            switch result {
            case .failure(let error):
                XCTFail(error.localizedDescription)
            case .success(let manager):
                XCTAssert(manager.deviceManager.isConnected, "Devices are not connected")
                XCTAssert(manager.deviceManager.isState(.unlocked), "Devices are still locked")
            }
            exp.fulfill()
        }
        wait(for: [exp], timeout: 1)
    }
    
    func testUnlock() throws {
        let manager = try XCTUnwrap(tripManager)
        XCTAssert(manager.deviceManager.isConnected, "Devices are not connected")
        unlock.expectedFulfillmentCount = 1
        manager.unlock()
        wait(for: [unlock], timeout: 3)
    }
    
    func testLock() throws {
        let manager = try XCTUnwrap(tripManager)
        XCTAssert(manager.deviceManager.isConnected, "Devices are not connected")
        lock.expectedFulfillmentCount = 1
        manager.lock(force: true)
        wait(for: [lock], timeout: 3)
    }
    
//    func testEndTrip() throws {
//        let manager = try XCTUnwrap(tripManager)
//        XCTAssert(manager.deviceManager.isConnected, "Devices are not connected")
//    }
    
    @objc
    fileprivate func handle(notification: Notification) {
        if let manager = tripManager?.deviceManager {
            if manager.isState(.locked) {
                lock.fulfill()
            }
            if manager.isState(.unlocked) {
                unlock.fulfill()
            }
        }
    }

    
    struct API: TripAPI, BikeAPI {
        func getTrip(by tripId: Int, completion: @escaping (Result<Trip, Error>) -> ()) {
            
        }
        
        func getTrips(completion: @escaping (Result<[Trip], Error>) -> ()) {

        }
        
        func startTrip(with bike: Bike, completion: @escaping (Result<Trip, Error>) -> ()) {
            completion(.success(.mock()))
//            completion(.failure(TripError.start))
        }
        
        func checkParkingFee(check: Parking.Check, completion: @escaping (Result<Parking.Fee, Error>) -> ()) {
            
        }
        
        func end(trip: Trip.End, completion: @escaping (Result<Trip, Error>) -> ()) {
            
        }
        
        func rate(trip: Trip.Rating, completion: @escaping (Result<Void, Error>) -> ()) {
            
        }
        
        func update(trip: Trip.Update, completion: @escaping (Result<Trip.Invoice, Error>) -> ()) {
            
        }
        
        func find(in region: MapRegion, completion: @escaping (Result<[Bike], Error>) -> ()) {
            
        }
        
        func getBike(by bikeId: Int?, qrCodeId: Int?, iotCode: String?, completion: @escaping (Result<Bike, Error>) -> ()) {
            
        }
        
        func book(bike: Bike, completion: @escaping (Result<(Bike.Booking, Status.Info), Error>) -> ()) {
            let info = Status.Info(trip: nil, booking: nil, operatorPhone: nil, supportPhone: nil)
            completion(.success((.mock(bike: bike), info)))
        }
        
        func cancelBooking(with info: Bike.Unbooking, completion: @escaping (Result<Void, Error>) -> ()) {
            
        }
        
        func unlock(bikeId: Int, completion: @escaping (Result<Void, Error>) -> ()) {
            
        }
        
        func lock(bikeId: Int, completion: @escaping (Result<Void, Error>) -> ()) {
            
        }
        
        func iotStatus(bikeId: Int, completion: @escaping (Result<IoTModule.Status, Error>) -> Void) {
            
        }
        
        func send(metadata: Metadata, completion: @escaping (Result<Void, Error>) -> ()) {
            
        }
    }
    
    enum TripError: Error {
        case start
    }
}
