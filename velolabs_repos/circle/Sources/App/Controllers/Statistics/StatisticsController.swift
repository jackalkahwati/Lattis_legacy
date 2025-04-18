//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 22.05.2021.
//

import Vapor
import Fluent
import Plot
import Foundation

struct StatisticsController: RouteCollection {
    
    func boot(routes: RoutesBuilder) throws {
        routes.get(use: statistics)
        routes.get("fleets", use: fleets)
        routes.get("weekend", use: weekendTransactions)
//        routes.get("trips", use: trips)
    }
    
    
    fileprivate func statistics(req: Request) throws -> EventLoopFuture<WebPage> {
        Bike.query(on: req.db(.main))
            .count()
            .flatMap { bikes in
                TripModel.query(on: req.db(.main))
                    .count()
                    .map { trips in
                        WebPage(
                            Div {
                                H3("Bikes: \(bikes)")
                                H3("Trips: \(trips)")
                            }
                        )
                    }
            }
    }
    
    fileprivate func fleets(req: Request) throws -> EventLoopFuture<WebPage> {
        Fleet.query(on: req.db(.main))
            .filter(\.$name != nil)
            .filter(\.$name != "")
            .with(\.$vehicles)
            .all()
            .map { fleets in
                let liveOnly: (Bike) -> Bool = { bike in
                    bike.status == .active && bike.name != nil && bike.name != ""
                }
                let total = fleets.flatMap(\.vehicles).filter(liveOnly).count
                let sorted = fleets.sorted { lhs, rhs in
                    lhs.vehicles.filter(liveOnly).count > rhs.vehicles.filter(liveOnly).count
                }
                return WebPage(
                    Node.body(
                        .h1("Live vehicles per fleet (\(total))"),
                        .forEach(sorted, { fleet in
                            .div(
                                .h3(.text(fleet.name!)),
                                .ol(
                                    .forEach(fleet.vehicles.filter(liveOnly), { vehicle in
                                        .li(.text(vehicle.name!))
                                    })
                                )
                            )
                        })
                    )
                )
            }
    }
    
    fileprivate func weekendTransactions(req: Request) throws -> EventLoopFuture<WebPage> {
        let fleetId = try req.query.get(Int.self, at: "fleetId")
        let fri = UInt(lastWeekDate(of: 6).timeIntervalSince1970)
        let sat = fri + 86400//.addingTimeInterval(86400)
        let sun = sat + 86400//.addingTimeInterval(86400)
        let mon = sun + 86400//.addingTimeInterval(86400)
//        print(fri.timeIntervalSince1970, sat.timeIntervalSince1970, sun.timeIntervalSince1970, mon.timeIntervalSince1970)
        return Trip.Receipt.query(on: req.db(.main))
            .filter(\.$fleetId == fleetId)
            .filter(\.$chargedAt > fri)
            .filter(\.$chargedAt < sat)
            .all()
            .flatMap { friday in
                Trip.Receipt.query(on: req.db(.main))
                    .filter(\.$fleetId == fleetId)
                    .filter(\.$chargedAt > sat)
                    .filter(\.$chargedAt < sun)
                    .all()
                    .flatMap { saturday in
                        Trip.Receipt.query(on: req.db(.main))
                            .filter(\.$fleetId == fleetId)
                            .filter(\.$chargedAt > sun)
                            .filter(\.$chargedAt < mon)
                            .all()
                            .map { sunday in
                                WebPage (
                                    Div {
                                        H3(statString(for: "Friday", transactions: friday))
                                        H3(statString(for: "Saturday", transactions: saturday))
                                        H3(statString(for: "Sunday", transactions: sunday))
                                    }
                                )
                            }
                    }
            }
    }
    
//    fileprivate func renderWebPage(trips: [Trip], fleets: [Fleet], users: [UserModel]) -> WebPage {
//        let formatter = DateFormatter()
//        formatter.dateFormat = "d MMM yyyy"
//        formatter.timeZone = .init(identifier: "CEST")
//        return WebPage (
//            Node.body (
//                .forEach(fleets, { fleet in
//                    let fTrips = trips.filter({$0.fleetId == fleet.id})
//                    return .div(
//                        .h3(.text(fleet.name! + " trips - \(fTrips.count)" + " charges - \(fTrips.compactMap(\.receipt?.total).reduce(0, +))" )),
//                        .table(
//                            .tr(
//                                .th("Start Date"),
//                                .th("End Date"),
//                                .th("First Name"),
//                                .th("Last Name"),
//                                .th("Email"),
//                                .th("Phone number"),
//                                .th("Start GPS"),
//                                .th("End GPS"),
//                                .th("Duration"),
//                                .th("Charge")
//                            ),
//                            .forEach(fTrips, { trip in
//                                let user = users.first(where: {$0.id == trip.userId})!
//                                return .tr(
//                                    .td(.text(formatter.string(uint: trip.createdAt))),
//                                    .td(.text(formatter.string(uint: trip.endedAt))),
//                                    .td(.text(user.firstName ?? "None")),
//                                    .td(.text(user.lastName ?? "None")),
//                                    .td(.text(user.email ?? "None")),
//                                    .td(.text(user.phoneNumber ?? "None")),
//                                    .td(.text(trip.startGPS)),
//                                    .td(.text(trip.endGPS)),
//                                    .td(.text(trip.duration)),
//                                    .td(.text(trip.charge))
//                                )
//                            })
//                        )
//                )
//                })
//            )
//        )
//    }
    
//    fileprivate func trips(req: Request) throws -> EventLoopFuture<WebPage> {
//        let fleetIds = try req.query.get([Int].self, at: "fleets") // 257,258,276,292,293
//        let start = "1 Aug 2021"
//        let end = "1 Oct 2021"
////        let mid = "1 Sep 2021"
//        let formatter = DateFormatter()
//        formatter.dateFormat = "d MMM yyyy"
//        formatter.timeZone = .init(identifier: "CEST")
//        let startDate = formatter.date(from: start)!
//        let endDate = formatter.date(from: end)!
//        return Trip.query(on: req.db(.main))
//            .filter(\.$fleetId ~~ fleetIds)
//            .filter(\.$createdAt > UInt(startDate.timeIntervalSince1970))
//            .filter(\.$createdAt < UInt(endDate.timeIntervalSince1970))
//            .with(\.$receipt)
//            .all()
//            .flatMap { trips in
//                Fleet.query(on: req.db(.main))
//                    .filter(\.$id ~~ fleetIds)
//                    .all()
//                    .flatMap { fleets in
//                        UserModel.query(on: req.db(.user))
//                            .filter(\.$id ~~ trips.map(\.userId))
//                            .all()
//                            .map { users in
//                                renderWebPage(trips: trips, fleets: fleets, users: users)
//                            }
//                    }
//            }
//    }
    
    fileprivate func lastWeekDate(of day: Int, timeZone: String = "PST") -> Date {
        let calendar = Calendar.current
        var components = calendar.dateComponents([.year, .month, .weekOfYear, .timeZone], from: Date())
        components.weekOfYear! -= 1
        components.weekday = day
        components.timeZone = .init(identifier: timeZone)
        return calendar.date(from: components)!
    }
    
    fileprivate func statString(for day: String, transactions: [Trip.Receipt]) -> String {
        let total = transactions.count
        let failed = transactions.filter({$0.transactionId == nil}).count
        if total <= 0 { return "\(day): No trips"}
        return "\(day): \(total) total, \(failed) failed, failure rate \((Double(failed)/Double(total))*100)%"
    }
}

struct WebPage: ResponseEncodable {
    let body: Component
    
    init(_ body: Component) {
        self.body = body
    }
    
    fileprivate func document() -> HTML {
        HTML(
            .component(body)
        )
    }
    
    func encodeResponse(for request: Request) -> EventLoopFuture<Response> {
        request.eventLoop.makeSucceededFuture(Response(status: .ok, version: .http1_1, headers: .init([("ContentType", "text/html")]), body: .init(string: document().render(indentedBy: .tabs(1)))))
    }
}

fileprivate extension DateFormatter {
    func string(uint: UInt?) -> String {
        guard let val = uint else { return "None" }
        return string(from: Date(timeIntervalSince1970: Double(val)))
    }
}

fileprivate extension TripModel {
    var startGPS: String {
        guard let val = stp.first, val.count > 1 else { return "None" }
        return "\(val[0]), \(val[1])"
    }
    
    var endGPS: String {
        guard let val = stp.last, val.count > 1 else { return "None" }
        return "\(val[0]), \(val[1])"
    }
    
    var stp: [[Double]] {
        guard let str = steps, let data = str.data(using: .utf8) else {return [[]]}
        do {
            return try JSONDecoder().decode([[Double]].self, from: data)
        } catch {
            return [[]]
        }
    }
    
    var duration: String {
        return "None"
//        guard let start = createdAt, let end = endedAt else { return "None" }
//        let formatter = DateComponentsFormatter()
//        formatter.unitsStyle = .short
//        formatter.allowedUnits = [.day, .hour, .minute]
//        return formatter.string(from: Double(end) - Double(start)) ?? "None"
    }
    
    var charge: String {
        guard let total = receipt?.total, let currency = receipt?.currency else { return "None" }
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.currencyCode = currency
        return formatter.string(from: NSNumber(value: total)) ?? "None"
    }
}

