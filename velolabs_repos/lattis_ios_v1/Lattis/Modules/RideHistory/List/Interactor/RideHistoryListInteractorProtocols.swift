//
//  RideHistoryListRideHistoryListInteractorProtocols.swift
//  Lattis
//
//  Created by Ravil Khusainov on 18/08/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Foundation

protocol RideHistoryListInteractorInput {
    var viewModel: RideHistoryListViewModel {get}
    func didSelectRow(at indexPath: IndexPath)
}

protocol RideHistoryListInteractorOutput: BaseInteractorOutput {

}
