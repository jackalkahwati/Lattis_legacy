//
//  HomeViewModel.swift
//  LeMond
//
//  Created by Ravil Khusainov on 01.01.2022.
//

import Foundation
import Combine

final class HomeViewModel: ObservableObject {
    
    @Published var secure: Bool
    let settings: AppSettings
    
    init(_ settings: AppSettings) {
        self.settings = settings
        self.secure = settings.secure
    }
    
    func toggleSecurity() {
        DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
            self.secure.toggle()
            self.settings.secure = self.secure
        }
    }
}
