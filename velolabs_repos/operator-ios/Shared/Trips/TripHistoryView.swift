//
//  TripHistoryView.swift
//  Operator
//
//  Created by Ravil Khusainov on 04.04.2021.
//

import SwiftUI

struct TripHistoryView: View {
    
    @StateObject var logic: TripHistoryLogicController
    
    var body: some View {
        ScrollView {
            LazyVStack {
                ForEach(logic.trips) { trip in
                    NavigationLink(destination: TripDetailsView(viewModel: .init(trip, vehicle: logic.vehicle)), label: {
                        TripListItem(trip: trip)
                    })
                    .buttonStyle(PlainButtonStyle())
                }
            }
            .padding()
        }
        .navigationTitle("Trips")
    }
}

//struct TripHistoryView_Previews: PreviewProvider {
//    static var previews: some View {
//        TripHistoryView()
//    }
//}
