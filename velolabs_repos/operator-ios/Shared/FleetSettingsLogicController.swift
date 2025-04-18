//
//  FleetSettingsLogicController.swift
//  Operator
//
//  Created by Ravil Khusainov on 05.03.2021.
//

import Foundation
import Combine


final class FleetSettingsLogicController: ObservableObject {
    
    @Published fileprivate(set) var fleets: [Fleet] = []
    fileprivate var cancellables: Set<AnyCancellable> = []
    
    init() {
        CircleAPI.fleets()
            .catch(fetchFailed)
            .assign(to: \.fleets, on: self)
            .store(in: &cancellables)
    }
    
    fileprivate func fetchFailed(_ error: Error) -> Just<[Fleet]> {
        print(error)
        return Just([])
    }
}
