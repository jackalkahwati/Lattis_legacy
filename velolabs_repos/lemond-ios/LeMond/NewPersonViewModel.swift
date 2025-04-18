//
//  NewPersonViewModel.swift
//  LeMond
//
//  Created by Ravil Khusainov on 04.01.2022.
//

import Foundation
import Combine
import SwiftUI

final class NewPersonViewModel: ObservableObject {
    
    let completion: (Person) -> Void
    
    init(_ completion: @escaping (Person) -> Void) {
        self.completion = completion
    }
    
    @Published var name: String = ""
    @Published var phoneNumber: String = ""
    
    var canShare: Bool {
        !name.isEmpty && !phoneNumber.isEmpty
    }
    
    func share() {
        completion(.init(id: UUID().uuidString, fullName: name, status: "Pending"))
    }
}
