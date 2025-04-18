//
//  RideHistoryListRideHistoryListInteractor.swift
//  Lattis
//
//  Created by Ravil Khusainov on 18/08/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Foundation

class RideHistoryListInteractor {
    weak var view: RideHistoryListInteractorOutput!
    var router: RideHistoryListRouter!
    let viewModel: RideHistoryListViewModel = RideHistoryListViewModel()
    
    init() {
        viewModel.onError = { [unowned self] error in
            self.view.show(error: error, file: #file, line: #line)
        }
    }
    
    deinit {
        viewModel.stop()
    }
}

extension RideHistoryListInteractor: RideHistoryListInteractorInput {
    func didSelectRow(at indexPath: IndexPath) {
        router.details(for: viewModel.trip(for: indexPath))
    }
}
