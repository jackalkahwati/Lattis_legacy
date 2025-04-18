//
//  AddBikeViewModel.swift
//  LeMond
//
//  Created by Ravil Khusainov on 04.01.2022.
//

import Foundation
import Combine
import UIKit


final class AddBikeViewModel: ObservableObject {
    
    @Published var scanning: Bool = true
    
    func orderBike() {
        UIApplication.shared.open(URL(string: "https://lemont.com")!)
    }
    
    func found(code: String) -> Bool {
        return false
    }
}
