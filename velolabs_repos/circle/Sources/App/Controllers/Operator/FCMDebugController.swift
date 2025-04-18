//
//  FCMDebugController.swift
//  
//
//  Created by Ravil Khusainov on 02.03.2021.
//

import Vapor
import FCM

struct FCMDebugController: RouteCollection {
    func boot(routes: RoutesBuilder) throws {
        let fcm = routes.grouped("fcm")
        fcm.post(use: sendPN)
    }

    fileprivate func sendPN(req: Request) throws -> EventLoopFuture<String> {
        struct Payload: Codable {
            let token: String
            let trip: Int
            let category: String
        }
        do {
            let body = try req.content.decode(Payload.self)
            let message = FCMMessage(token: body.token, notification: nil)
            message.apns = .init(
                payload: .init(
                    aps: .init(
                        sound: "default",
                        priority: "high",
                        contentAvailable: true
//                        category: body.category
                    )
                )
            )
            message.data = [
                "trip_id": "\(body.trip)",
                "action": body.category
            ]
            return req.fcm.send(message)
                .transform(to: "Notification sent")
        } catch {
            print(error)
            throw Abort(.conflict, reason: error.localizedDescription)
        }
    }

    fileprivate func blitz(req: Request) throws -> EventLoopFuture<String> {
        let vehicle = Kuhmute.Dock(vehicle_uuid: "na-est-2.veh_b32d64cd-b745-4be3-bda5-a64112114802",
                                   hub_uuid: "na_est_2.hub_33eab7e7-57b0-4e4f-abb5-dd8a8837c7e2",
                                   port: 2)
        do {
            return try Kuhmute.dock(vehicle: vehicle, with: req.client)
                .transform(to: "Vehicle is docked")
        } catch {
            throw Abort(.conflict, reason: error.localizedDescription)
        }
    }
}

func fcm(_ app: Application) {
    app.fcm.configuration = .envServiceAccountKeyFields
    app.fcm.configuration?.apnsDefaultConfig = FCMApnsConfig(headers: [:],
                                                             aps: FCMApnsApsObject(sound: ""))
}
