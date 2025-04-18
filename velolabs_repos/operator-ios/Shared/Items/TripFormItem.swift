//
//  TripFormItem.swift
//  Operator
//
//  Created by Ravil Khusainov on 03.04.2021.
//

import SwiftUI

struct TripListItem:View {
    let trip: Trip
    var body: some View {
        TripFormItem(trip: trip)
            .padding()
            .background(Color.accentColor.opacity(0.3))
            .cornerRadius(10)
    }
}

struct TripFormItem: View {
    let trip: Trip
    var body: some View {
        HStack {
            VStack(alignment: .leading) {
                if trip.endedAt == nil {
                    Text("Active trip")
                        .font(.footnote)
                        .foregroundColor(.accentColor)
                }
                Text(trip.user.fullName)
                Text(DateFormatter.default.string(from: trip.createdAt))
                    .font(.footnote)
            }
            Spacer()
            if let duration = trip.duration {
                Text(duration)
            }
        }
    }
}

//struct TripFormItem_Previews: PreviewProvider {
//    static var previews: some View {
//        TripFormItem()
//    }
//}
