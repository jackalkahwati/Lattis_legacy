//
//  EquipmentPageLogicController.swift
//  Operator
//
//  Created by Ravil Khusainov on 13.04.2021.
//

import Foundation

final class EquipmentPageLogicController: ObservableObject {
    
    @Published fileprivate(set) var equipment: [Thing] = []
    var selectedThing: Thing?
    var mainThing: Thing? {
        return equipment.first
    }
    
    var otherThings: [Thing] {
        guard !equipment.isEmpty else { return equipment }
        var res = equipment
        res.removeFirst()
        return res
    }
    
    init(_ equipment: [Thing]) {
        self.equipment = equipment
    }
}
