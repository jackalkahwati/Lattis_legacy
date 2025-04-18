//
//  TicketNetwork.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 5/16/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Oval
import CoreLocation

protocol TicketNetwork {
    func getTickets(for fleet: Fleet, completion: @escaping (Result<[Ticket], Error>) -> ())
    func create(ticket: Ticket.Create, completion: @escaping (Result<Void, Error>) -> ())
    func resolve(ticket: Ticket, completion: @escaping (Result<Void, Error>) -> ())
    func assign(oper: Operator,to ticket: Ticket, completion: @escaping (Result<Void, Error>) -> ())
}

fileprivate extension API {
    static func ticket(_ path: String) -> API {
        return .init(path: "ticket/" + path)
//        return .init(endpoint: "http://192.168.2.40:3001/api", path: "ticket/" + path)
    }
    static let getTickets = ticket("get-tickets")
    static let create = ticket("create-ticket")
    static let resolve = ticket("resolve-ticket")
    static let assign = ticket("assign-ticket")
}

extension Session: TicketNetwork {
    func getTickets(for fleet: Fleet, completion: @escaping (Result<[Ticket], Error>) -> ()) {
        send(.post(json: fleet, api: .getTickets), completion: completion)
    }
    
    func create(ticket: Ticket.Create, completion: @escaping (Result<Void, Error>) -> ()) {
        send(.post(json: ticket, api: .create), completion: completion)
    }
    
    func resolve(ticket: Ticket, completion: @escaping (Result<Void, Error>) -> ()) {
        send(.post(json: ticket, api: .resolve), completion: completion)
    }
    
    func assign(oper: Operator, to ticket: Ticket, completion: @escaping (Result<Void, Error>) -> ()) {
        struct Params: Encodable {
            let operatorId: Int
            let ticketId: Int
        }
        let params = Params(operatorId: oper.operatorId, ticketId: ticket.ticketId)
        send(.post(json: params, api: .assign), completion: completion)
    }
}



