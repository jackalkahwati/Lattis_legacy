//
//  TheftTheftInteractorProtocols.swift
//  Lattis
//
//  Created by Ravil Khusainov on 04/04/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Foundation

protocol TheftInteractorInput {
    func submit()
}

protocol TheftInteractorOutput: BaseInteractorOutput {
    func showSuccess()
}
