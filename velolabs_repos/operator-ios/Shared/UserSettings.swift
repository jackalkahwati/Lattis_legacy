//
//  UserSettings.swift
//  Operator
//
//  Created by Ravil Khusainov on 23.02.2021.
//

import SwiftUI
import Combine
import Wrappers
import Firebase

class UserSettings: ObservableObject {
    @Published var fleet: Fleet = .select {
        didSet {
            guard fleet != .select else { return }
            storage.save(fleet)
            fetchColleagues()
            inject.fleet.send(fleet)
            inject.mapLogic.refresh(clenCache: true)
//            inject.ticketsLogic.fetch()
//            inject.vehiclesLogic.fetch()
        }
    }
    @Published var user: FleetOperator = .unassigned {
        didSet {
            guard user != .unassigned else { return }
            userId = user.id
            storage.save(user)
        }
    }
    @Published var appState: AppState = .splash
    fileprivate(set) var inject: Dependency = .init()
    
    fileprivate(set) var storage: JSONStorage = .shared
    
    @KeychainBacked(key: "api_token")
    fileprivate var token: String?
    @AppStorage("user_id")
    fileprivate var userId: Int?
    fileprivate var cancellables: Set<AnyCancellable> = []
    
    init() {
        FirebaseApp.configure()
        inject.ticketsLogic = .init(self)
        inject.vehiclesLogic = .init(self)
        inject.mapLogic = .init(settings: self)
    }
    
    
    func logOut() {
        storage.destroy()
        token = nil
        CircleAPI.logOut()
        userId = nil
        fleet = .select
        user = .unassigned
        appState = .signIn
    }
    
    func loggedIn(auth: FleetOperator.Auth) {
        token = auth.token
        CircleAPI.logIn(auth.token)
        storage = .init(documents: "user_\(auth.operator.id)")
        user = auth.operator
        fetchFleet()
        Analytics.set(user: auth.operator.id)
    }
    
    func checkCurrentStatus() {
        if let token = token, !token.isEmpty, let userId = userId {
            Analytics.set(user: userId)
            CircleAPI.logIn(token)
            storage = .init(documents: "user_\(userId)")
            storage.fetch(type: FleetOperator.self)
                .sink { (result) in
                    switch result {
                    case .failure:
                        self.logOut()
                    case .finished:
                        self.fetchFleet()
                    }
                } receiveValue: { user in
                    self.user = user
                }
                .store(in: &cancellables)
        } else {
            logOut()
        }
    }
    
    fileprivate func fetchFleet() {
        storage.fetch(type: Fleet.self)
            .sink { (result) in
                switch result {
                case .failure:
                    self.appState = .fleet
                case .finished:
                    self.appState = .main
                }
            } receiveValue: { fleet in
                self.fleet = fleet
            }
            .store(in: &cancellables)
    }
    
    fileprivate func fetchColleagues() {
        CircleAPI.colleagues(fleetId: fleet.id)
            .sink { result in
            } receiveValue: { coll in
                self.storage.save(coll)
            }
            .store(in: &cancellables)

    }
}

extension UserSettings {
    enum AppState {
        case splash
        case signIn
        case main
        case fleet
    }
    
    struct Dependency {
        let vehicle = PassthroughSubject<Vehicle, Never>()
        let ticket = PassthroughSubject<Ticket, Never>()
        let fleet = PassthroughSubject<Fleet, Never>()
        var ticketsLogic: TicketsLogicController!
        var vehiclesLogic: VehiclesLogicController!
        var mapLogic: VehiclesListMapViewModel!
    }
}
