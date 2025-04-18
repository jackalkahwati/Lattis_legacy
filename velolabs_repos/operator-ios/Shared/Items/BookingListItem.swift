//
//  BookingListItem.swift
//  Operator
//
//  Created by Ravil Khusainov on 14.06.2021.
//

import SwiftUI

struct BookingListItem: View {
    let booking: Vehicle.Booking
    var body: some View {
        HStack {
            VStack(alignment: .leading) {
                if booking.finishedAt == nil {
                    Text("Active booking")
                        .font(.footnote)
                        .foregroundColor(.accentColor)
                }
                Text(booking.user.fullName)
                Text(DateFormatter.default.string(from: booking.startedAt))
                    .font(.footnote)
            }
            Spacer()
            if let duration = booking.duration {
                Text(duration)
            }
        }
    }
}

//struct BookingListItem_Previews: PreviewProvider {
//    static var previews: some View {
//        BookingListItem()
//    }
//}
