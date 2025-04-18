//
//  FleetItem.swift
//  Operator
//
//  Created by Ravil Khusainov on 23.02.2021.
//

import SwiftUI
import URLImage

struct FleetItem: View {
    
    let fleet: Fleet
    let selected: Bool
    
    var body: some View {
        HStack {
            if let url = fleet.logo {
                URLImage(url: url) { (image) in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 50, height: 50)
                }
                .padding(10)
            }
            VStack(alignment: .leading) {
                Text(fleet.name ?? "No name")
                if let address = fleet.fullAddress {
                    Text(address)
                        .foregroundColor(.secondary)
                }
                Text("Vehicles: \(fleet.vehiclesCount)")
                    .font(.footnote)
                    .foregroundColor(.blue)
            }
            Spacer()
            if selected {
                Image(systemName: "checkmark")
                    .padding(8)
                    .font(.title2)
            }
        }
        .padding(8)
        .background(Color.accentColor.opacity(selected ? 0.5 : 0.1))
        .cornerRadius(10.0)
        .padding(.horizontal)
    }
}

struct FleetItem_Previews: PreviewProvider {
    static var previews: some View {
        FleetItem(fleet: .select, selected: false)
    }
}
