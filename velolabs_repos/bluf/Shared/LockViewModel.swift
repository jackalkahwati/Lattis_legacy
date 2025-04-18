//
//  LockViewModel.swift
//  BLUF
//
//  Created by Ravil Khusainov on 22.08.2020.
//

import SwiftUI
import Combine

final class LocksViewModel: ObservableObject {
    @Published var macId: String = ""
    @Published fileprivate(set) var locks: [EllipseLock] = []
    fileprivate var token: AnyCancellable?
    
//    init() {
//        #if DEBUG
//        locks = .stabs
//        #endif
//    }
    
    func fetch() {
        token = CircleAPI.locks()
            .map({$0.map(EllipseLock.init)})
            .catch(fetchFailed)
            .assign(to: \.locks, on: self)
    }
    
    func search() {
        
    }
    
    fileprivate func fetchFailed(_ error: Error) -> Just<[EllipseLock]> {
        print(error)
        return Just([])
    }
}

