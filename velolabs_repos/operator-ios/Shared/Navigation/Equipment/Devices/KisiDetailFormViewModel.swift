//
//  KisiDetailFormViewModel.swift
//  Operator
//
//  Created by Ravil Khusainov on 24.11.2021.
//

import Combine

final class KisiDetailFormViewModel: ObservableObject {
    
    let thing: Thing
    @Published var device: KisiDevice.Lock?
    var unlocking: Bool = false
    
    fileprivate var storage: Set<AnyCancellable> = []
    
    init(_ thing: Thing) {
        self.thing = thing
        connect()
    }
    
    func connect() {
        CircleAPI.device(thing)
            .sink { result in
                switch result {
                case .failure(let error):
                    Analytics.report(.error(error))
                case .finished:
                    break
                }
            } receiveValue: { dev in
                self.device = dev
            }
            .store(in: &storage)
    }
    
    func unlock() {
        unlocking = true
        CircleAPI.unlock(thing)
            .sink { result in
                switch result {
                case .failure(let error):
                    Analytics.report(.error(error))
                case .finished:
                    break
                }
            } receiveValue: {
                self.unlocking = false
            }
            .store(in: &storage)
    }
}
