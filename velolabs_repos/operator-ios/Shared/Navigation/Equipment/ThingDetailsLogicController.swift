//
//  ThingDetailsLogicController.swift
//  Operator
//
//  Created by Ravil Khusainov on 11.03.2021.
//

import Combine
import Foundation

final class ThingDetailsLogicController: ObservableObject {
    
    let thing: Thing
    
    var key: String { thing.metadata.key }
    var deviceType: String { thing.metadata.deviceType }
    
    @Published var viewState: ThingDetailView.ViewState = .status
    @Published var coverState: ThingDetailView.ControlState = .standby
    @Published var isFetching: Bool = false
    
    fileprivate(set) var status: Thing.Status?
    fileprivate let device: PhysicalDevice?
    fileprivate var cancellables: Set<AnyCancellable> = []
    
    init(_ thing: Thing) {
        self.thing = thing
        self.device = Device.physicalDevice(from: thing)
        fetch()
        setupHandlers()
    }
    
    deinit {
        cancellables.forEach{$0.cancel()}
    }
    
    fileprivate func setupHandlers() {
        device?.link
            .sink { result in
                switch result {
                case .failure(let error):
                    self.viewState = .statusFailed
                case .finished:
                    break
                }
            } receiveValue: { link in
                self.handle(link)
            }
            .store(in: &cancellables)
        device?.security
            .sink { result in
                
            } receiveValue: { security in
                self.handle(security)
            }
            .store(in: &cancellables)
    }
    
    fileprivate func handle(_ link: Device.Link) {
        isFetching = false
    }
    
    fileprivate func handle(_ security: Device.Security) {
        switch security {
        case .locked:
            viewState = .locked
        case .unlocked:
            viewState = .unlocked
        default:
            break
        }
    }
    
    func fetch() {
        isFetching = true
        if let device = device {
            return device.connect()
        }
        CircleAPI.status(thingId: thing.id)
            .sink { [weak self] (result) in
                switch result {
                case .failure(let error):
                    print(error)
                    self?.viewState = .statusFailed
                case .finished:
                    self?.isFetching = false
                }
            } receiveValue: { [weak self] (status) in
                if status.locked {
                    self?.viewState = .locked
                } else {
                    self?.viewState = .unlocked
                }
                self?.status = status
            }
            .store(in: &cancellables)
    }
    
    func toggleLock() {
        if let device = device {
            viewState = .processing
            return device.toggle()
        }
        switch viewState {
        case .locked:
            CircleAPI.unlock(thingId: thing.id)
                .sink { [weak self] (result) in
                    switch result {
                    case .failure(let error):
                        print(error)
                        self?.viewState = .locked
                    case .finished:
                        break
                    }
                } receiveValue: { [weak self] message in
                    if let linka = message.linka {
                        self?.trackLinka(command: linka.command_id)
                    } else {
                        self?.viewState = .unlocked
                    }
                }
                .store(in: &cancellables)
        case .unlocked:
            CircleAPI.lock(thingId: thing.id)
                .sink { [weak self] (result) in
                    switch result {
                    case .failure(let error):
                        print(error)
                        self?.viewState = .unlocked
                    case .finished:
                        break
                    }
                } receiveValue: { [weak self] message in
                    if let linka = message.linka {
                        self?.trackLinka(command: linka.command_id)
                    } else {
                        self?.viewState = .locked
                    }
                }
                .store(in: &cancellables)
        default:
            break
        }
        viewState = .processing
    }
    
    fileprivate func trackLinka(command: String) {
        guard let fleet = thing.metadata.fleetId else { return }
        CircleAPI.trackLinka(command: command, fleetId: fleet)
            .sink { result in
                switch result {
                case .failure(let error):
                    print(error)
                    self.viewState = .locked
                case .finished:
                    break
                }
            } receiveValue: { [weak self] info in
                if info.status == .finished {
                    self?.viewState = info.command == "LOCK" ? .locked : .unlocked
                } else {
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                        self?.trackLinka(command: command)
                    }
                }
            }
            .store(in: &cancellables)
    }
    
    func uncoverBattery() {
        coverState = .processing
        CircleAPI.uncover(thingId: thing.id)
            .sink { [weak self] result in
                switch result {
                case .finished:
                    self?.coverState = .standby
                case .failure(let error):
                    if error.isHTTP(.conflict) {
                        self?.coverState = .notSupported
                    } else {
                        self?.coverState = .failed
                    }
                }
            } receiveValue: {}
            .store(in: &cancellables)
    }
    
    func control(light: Thing.Lighth) {
        CircleAPI.control(light: light, thingId: thing.id)
            .sink { result in
                switch result {
                case .finished:
                    print("Light: Success")
                case .failure(let error):
                    print(error)
                }
            } receiveValue: {}
            .store(in: &cancellables)
    }
    
    func control(sound: Thing.Sound) {
        CircleAPI.control(sound: sound, thingId: thing.id)
            .sink { result in
                switch result {
                case .finished:
                    print("Sound: Success")
                case .failure(let error):
                    print(error)
                }
            } receiveValue: {}
            .store(in: &cancellables)
    }
    
    func unassign() {
        
    }
}
