//
//  VehicleListItem.swift
//  Operator
//
//  Created by Ravil Khusainov on 01.03.2021.
//

import SwiftUI

struct VehicleListItem: View {
    
    @State var vehicle: Vehicle
    
    var body: some View {
        HStack {
            VStack(alignment: .leading) {
                Text(vehicle.name)
                Text(vehicle.metadata.group.type.title)
                    .font(.footnote)
                    .foregroundColor(.secondary)
            }
            Spacer()
            VStack(alignment: .trailing) {
                Text(vehicle.status)
                    .foregroundColor(.secondary)
                Text(vehicle.metadata.usage.displayValue)
                    .font(.footnote)
            }
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color.accentColor.opacity(0.5))
        .cornerRadius(10)
    }
}

struct VehicleListItem_Previews: PreviewProvider {
    static var previews: some View {
        VehicleListItem(vehicle: [Vehicle].dummy.first!)
    }
}
