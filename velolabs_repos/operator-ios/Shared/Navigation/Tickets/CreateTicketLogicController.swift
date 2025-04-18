//
//  CreateTicketLogicController.swift
//  Operator
//
//  Created by Ravil Khusainov on 22.03.2021.
//

import Combine

final class CreateTicketLogicController: ObservableObject {
    
    let settings: UserSettings
    let ticketCreated: PassthroughSubject<Ticket, Never>
    let vehicleSelected = PassthroughSubject<Vehicle, Never>()
    let canChangeVehicle: Bool
    @Published var viewState: ViewState = .screen
    @Published fileprivate(set) var colleagues: [FleetOperator] = []
    @Published var notes: String?
    @Published var vehicle: Vehicle?
    @Published var category: Ticket.Category?
    @Published var assignee: FleetOperator?
    fileprivate var cancellables: Set<AnyCancellable> = []
    
    var vehicleName: String { vehicle?.name ?? "Vehicle" }
    var categoryImage: String { category?.imageName ?? "questionmark.circle" }
    var fleetId: Int { settings.fleet.id }
    
    init(_ settings: UserSettings, created: PassthroughSubject<Ticket, Never>, vehicle: Vehicle? = nil) {
        self.settings = settings
        self.ticketCreated = created
        self.vehicle = vehicle
        self.canChangeVehicle = vehicle == nil
        fetchColleagues()
        vehicleSelected.sink { [weak self] vehicle in
            self?.vehicle = vehicle
        }
        .store(in: &cancellables)
    }
    
    func validate() -> Bool {
        vehicle != nil && category != nil
    }
    
    func fetchColleagues() {
//        CircleAPI.colleagues(fleetId: settings.fleet.id)
        settings.storage.fetch(type: [FleetOperator].self)
            .sink { [weak self] (com) in
                switch com {
                case .failure(let error):
                    self?.viewState = .error("Warning", error.localizedDescription)
                default: break
                }
            } receiveValue: { [weak self] (col) in
                self?.colleagues = col
            }
            .store(in: &cancellables)
    }
    
    func createTicket(completion: @escaping () -> Void) {
        guard let vehicle = vehicle, let category = category else { return }
        let ticket = Ticket.Create(category: category, assignee: assignee?.id, fleetId: settings.fleet.id, vehicle: vehicle.id, notes: notes, createdBy: settings.user.id)
        viewState = .loading
        CircleAPI.create(ticket: ticket)
            .sink { [weak self] (com) in
                switch com {
                case .failure(let error):
                    self?.viewState = .error("Warning", error.localizedDescription)
                case .finished:
                    completion()
                }
            } receiveValue: { [weak self] (meta) in
                self?.ticketCreated.send(.init(metadata: meta))
            }
            .store(in: &cancellables)
    }
}
