//
//  BikesViewModel.swift
//  BLUF
//
//  Created by Ravil Khusainov on 12.11.2020.
//

import SwiftUI
import Combine

final class BikesViewModel: ObservableObject {
    
    @Published var filters: [Bike.Filter] = []
    @Published fileprivate(set) var bikes: [Bike] = []
    fileprivate var token: AnyCancellable?
    
    func fetch() {
        token = CircleAPI.bikes(filters)
            .catch(fetchFailed)
            .assign(to: \.bikes, on: self)
    }
    
    fileprivate func fetchFailed(_ error: Error) -> Just<[Bike]> {
        print(error)
        return Just([])
    }
}
