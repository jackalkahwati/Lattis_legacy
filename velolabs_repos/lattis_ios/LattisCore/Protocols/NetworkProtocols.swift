//
//  NetworkProtocols.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 25/06/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import CoreLocation
import Model

struct MapRegion {
    let ne: CLLocationCoordinate2D
    let sw: CLLocationCoordinate2D
    
    func contains(_ point: CLLocationCoordinate2D) -> Bool {
        return point.latitude > sw.latitude && point.latitude < ne.latitude && point.longitude < ne.longitude && point.longitude > sw.longitude
    }
}

protocol BikeAPI {
    func find(in region: MapRegion, completion: @escaping (Result<[Bike], Error>) -> ())
//    func find(in coordinates: CLLocationCoordinate2D, completion: @escaping (Result<[Bike], Error>) -> ())
    func getBike(by bikeId: Int?, qrCodeId: Int?, iotCode: String? , completion: @escaping (Result<Bike, Error>) -> ())
    func book(bike: Bike, pricingId: Int?, completion: @escaping (Result<Bike.Booking, Error>) -> ())
    func cancelBooking(with info: Bike.Unbooking, completion: @escaping (Result<Void, Error>) -> ())
    func unlock(bikeId: Int, controllers: [String], completion: @escaping (Result<String?, Error>) -> ())
    func lock(bikeId: Int, controllers: [String], completion: @escaping (Result<String?, Error>) -> ())
    func iotStatus(bikeId: Int, key: String, completion: @escaping (Result<Thing.Status, Error>) -> Void)
    func lockStatus(bikeId: Int, commandId: String, completion: @escaping (Result<Thing.CommandFeedback, Error>) -> Void)
    func send(metadata: Metadata, completion: @escaping (Result<Void, Error>) -> ())
}

protocol TripAPI {
    func getTrip(by tripId: Int, completion: @escaping (Result<Trip.Details, Error>) -> ())
    func getTrips(completion: @escaping (Result<[Trip], Error>) -> ())
    func startTrip(with start: Trip.Start, completion: @escaping (Result<Trip, Error>) -> ())
    func checkParkingFee(check: Parking.Check, completion: @escaping (Result<Parking.Fee, Error>) -> ())
    func end(trip: Trip.End, completion: @escaping (Result<Trip, Error>) -> ())
    func rate(trip: Trip.Rating, completion: @escaping (Result<Void, Error>) -> ())
    func update(trip: Trip.Update, completion: @escaping (Result<Trip.Invoice, Error>) -> ())
}

protocol UserAPI {
    func checkStatus(completion: @escaping (Result<Status.Info, Error>) -> ())
    func refresh(completion: @escaping (Result<User.Update, Error>) -> ())
    func logIn(user: User.LogIn, completion: @escaping (Result<Bool, Error>) -> ())
    func addPrivateNetwork(email: String, code: String?, completion: @escaping (Result<[Fleet]?, Error>) -> ())
    func update(user: User, completion: @escaping (Result<User, Error>) -> ())
    func update(email: String, code: String?, completion: @escaping (Result<User?, Error>) -> ())
    func update(phone: String, code: String?, completion: @escaping (Result<User?, Error>) -> ())
    func update(password: String, newPassword: String, completion: @escaping (Result<Void, Error>) -> ())
    func verify(email: String, code: String?, completion: @escaping (Result<Void, Error>) -> ())
    func restorePasswrd(email: String, code: String?, password: String?, completion: @escaping (Result<Void, Error>) -> ())
    func deleteAccount(completion: @escaping (Result<User?, Error>) -> ())
}

protocol AppsAPI {
    func fetchInfo(completion: @escaping (Result<AppInfo, Error>) -> ())
}

protocol HubsAPI {
    func fetchRentals(in region: MapRegion, completion: @escaping (Result<Rentals, Error>) -> Void) -> URLSessionTask?
    func undock(vehicle: Bike, completion: @escaping (Result<Void, Error>) -> Void)
    func filter(bikeName: String, completion: @escaping (Result<[Bike], Error>) -> Void) -> URLSessionTask?
    func find(by qrCode: String) async throws -> Rental
}

protocol PromotionAPI {
    func fetchPromotions(completion: @escaping (Result<[Promotion], Error>) -> Void)
    func redeem(promoCode: String, completion: @escaping (Result<Promotion, Error>) -> Void)
}

protocol PaymentNetwork {
    func createIntent(request: Payment.Intent.Request, completion: @escaping (Result<String, Error>) -> ())
    func add(card: Payment.Card.New, completion: @escaping (Result<Void, Error>) -> ())
    func add(mpCard: MercadoPago.TokenizedCard, completion: @escaping (Result<Void, Error>) -> ())
    func getCards(completion: @escaping (Result<[Payment.Card], Error>) -> ())
    func delete(card: Payment.Card, completion: @escaping (Result<Void, Error>) -> ())
    func update(card: Payment.Card.Update, completion: @escaping (Result<Void, Error>) -> ())
    func setPrimary(card: Payment.Card, completion: @escaping (Result<Void, Error>) -> ())
}

protocol MPPaymentAPI {
    func tokenize(_ card: MercadoPago.CustomerCard, with publicKey: String, completion: @escaping (Result<String, Error>) -> ())
}

protocol ServiceNetwork {
    func report(damage: Damage, completion: @escaping (Result<Void, Error>) -> ())
    func report(theft: Theft, completion: @escaping (Result<Void, Error>) -> ())
}

protocol FleetsNetwork {
    func fetchFleets(completion: @escaping (Result<[Model.Fleet], Error>) -> ())
}

enum UploadType: String {
    case parking, maintenance, bike
}

protocol FileNetwork {
    func upload(data: Data, for type: UploadType, completion: @escaping (Result<URL, Error>) -> ())
}

protocol ParkingAPI {
    func getParkings(by fleetId: Int, bikeId: Int?, coordinate: CLLocationCoordinate2D?, completion: @escaping (Result<Parking, Error>) -> ())
}

protocol ReservationsNetwork {
    func estimate(request: Reservation.Request, completion: @escaping (Result<Reservation.Estimate, Error>) -> ())
    func fetch(by bikeId: Int, completion: @escaping (Result<[Reservation], Error>) -> ())
    func fetchReservations(completion: @escaping (Result<[Reservation], Error>) -> ())
    func fetchBikes(request: Reservation.BikesRequest, completion: @escaping (Result<[Model.Bike], Error>) -> ())
    func nextReservation(by bikeId: Int, completion: @escaping (Result<Reservation?, Error>) -> ())
    func createReservation(reques: Reservation.Request, completion: @escaping (Result<Reservation, Error>) -> ())
    func cancel(reservation: Reservation, completion: @escaping (Result<Reservation, Error>) -> ())
    func startTrip(reservation: Reservation, completion: @escaping (Result<Trip, Error>) -> ())
}

protocol SubscriptionsAPI {
    func fetchMemberships(completion: @escaping (Result<[Membership], Error>) -> ())
    func fetchSubscriptions(completion: @escaping (Result<[Subscription], Error>) -> ())
    func subscribe(to membership: Membership, completion: @escaping (Result<Subscription, Error>) -> ())
    func unsubscribe(from membership: Membership, completion: @escaping (Result<Subscription, Error>) -> ())
}

protocol GeofenceAPI {
    func fetch(by fleetId: Int, completion: @escaping (Result<[Geofence], Error>) -> ())
}

