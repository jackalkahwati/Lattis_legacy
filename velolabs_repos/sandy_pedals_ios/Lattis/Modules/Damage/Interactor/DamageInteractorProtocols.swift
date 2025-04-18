//
//  DamageDamageInteractorProtocols.swift
//  Lattis
//
//  Created by Ravil Khusainov on 30/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

protocol DamageInteractorInput {
    func didMake(picture: UIImage)
    func didSelect(category: DamageReport.Category)
    func submit(with note: String?)
    
    func cancelBooking()
    func endRide()
}

protocol DamageInteractorOutput: BaseInteractorOutput {
    func setSubmition(enabled: Bool)
    func showSuccess()
}
