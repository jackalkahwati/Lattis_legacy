//
//  SettingsViewModel.swift
//  LeMond
//
//  Created by Ravil Khusainov on 04.01.2022.
//

import Combine
import Foundation

final class SettingsViewModel: ObservableObject {
    
    let appSettings: AppSettings
    
    init(_ settings: AppSettings) {
        self.appSettings = settings
    }
    
    @Published var sharedWith: [Person] = [
        .init(id: UUID().uuidString, fullName: "Jeremy Ricard", status: "Accepted"),
        .init(id: UUID().uuidString, fullName: "Jack Al-Kahwati", status: "Pending"),
    ]
    @Published var revokePerson: Person?
    @Published var sheetState: SheetState?
    
    var bikeOwner: String { appSettings.name }
    
    func revokeAccess(_ person: Person) {
        if let idx = sharedWith.firstIndex(where: {$0.id == person.id}) {
            sharedWith.remove(at: idx)
        }
    }
    
    func granAccess(_ person: Person) {
        sharedWith.append(person)
    }
}

extension SettingsViewModel {
    enum SheetState: Identifiable {
        case share
        
        var id: Self { self }
    }
}

struct Person: Identifiable {
    let id: String
    let fullName: String
    let status: String
}
