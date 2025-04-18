//
//  FleetsLogicController.swift
//  Operator
//
//  Created by Ravil Khusainov on 05.03.2021.
//

import Foundation
import Combine

final class FleetsLogicController: ObservableObject {
    
    @Published var selected: Fleet
    @Published var viewState: ViewState = .loading
    @Published var searchName: String = ""
    @Published fileprivate(set) var fleets: [Fleet] = .dummy
    var userName: String { settings.user.fullName }
    fileprivate var allFleets: [Fleet] = []
    fileprivate let settings: UserSettings
    fileprivate var cancellables: Set<AnyCancellable> = []
    
    init(_ settings: UserSettings) {
        self.settings = settings
        self.selected = settings.fleet
//        fetch()
    }
    
    func fetch() {
        self.selected = settings.fleet
        CircleAPI.fleets()
            .catch(fetchFailed)
            .sink { [unowned self] fleets in
                allFleets = fleets.sorted(by: {$0.vehiclesCount > $1.vehiclesCount})
                aplyFilter()
                viewState = .screen
            }
            .store(in: &cancellables)
    }
    
    func aplyFilter() {
        if searchName.isEmpty {
            fleets = allFleets
        } else {
            fleets = allFleets.filter({$0.name?.lowercased().contains(self.searchName.lowercased()) ?? false})
        }
    }
    
    func validate() -> Bool { selected != .select }
    
    func done() {
        settings.fleet = selected
        cancel()
    }
    
    func cancel() {
        settings.appState = .main
    }
    
    func logout() {
        settings.logOut()
    }
    
    fileprivate func fetchFailed(_ error: Error) -> Just<[Fleet]> {
        viewState = .error(nil, error.localizedDescription)
        return Just([])
    }
}
