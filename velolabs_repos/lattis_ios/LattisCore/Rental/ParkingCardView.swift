//
//  ParkingCardView.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 2022-05-23.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import SwiftUI
import Model
import Kingfisher

struct ParkingCardView: View {
    
    let spot: Parking.Spot
    let previousCard: AssetDashboardView.Card?
    @Binding var card: AssetDashboardView.Card
    @State var directions: Bool = false
    @Environment(\.openURL) var openURL
    
    var body: some View {
        VStack(alignment: .leading, spacing: .margin/2) {
            Text(spot.name)
                .font(.theme(weight: .bold, size: .body))
            HStack {
                if let descr = spot.details {
                    Text(descr)
                        .font(.theme(weight: .book, size: .text))
                }
                KFImage(spot.pic)
                    .resizable()
                    .aspectRatio(contentMode: .fill)
                    .frame(width: 88, height: 88)
                    .cornerRadius(10)
                    .overlay(
                        RoundedRectangle(cornerRadius: 10)
                            .stroke(lineWidth: 2)
                    )
            }
            FlowControlView(negative: ("close", {
                guard let last = previousCard else { return }
                card = last
            }), positive: ("parking_get_direction", {
                if actionButtons.count == 1 {
                    openMap()
                } else {
                    directions = true
                }
            }))
            .actionSheet(isPresented: $directions) {
                ActionSheet(title: Text(spot.name), buttons: actionButtons)
            }
        }
        .foregroundColor(.black)
        .padding()
        .background(
            Color.white
                .cornerRadius(.containerCornerRadius, corners: [.topLeft, .topRight])
                .shadow(color: .black.opacity(0.11), radius: 5, y: -5)
        )
        
    }
    
    fileprivate var actionButtons: [ActionSheet.Button] {
        var buttons: [ActionSheet.Button] = [
            .default(Text("Apple Maps"), action: openMap)
        ]
        if UIApplication.shared.canOpenURL(URL(string: "https://www.google.com/maps")!) {
            buttons.append(.default(Text("Google Maps"), action: {
                var urlString = "https://www.google.com/maps/?daddr=\(spot.coordinate.latitude),\(spot.coordinate.longitude)"
                urlString += "&q=\(spot.name.addingPercentEncoding(withAllowedCharacters: .alphanumerics)!)"
                let url = URL(string: urlString)!
                openURL(url)
            }))
            buttons.append(.cancel())
        }
        return buttons
    }
    
    fileprivate func openMap() {
        var urlString = "https://maps.apple.com/?daddr=\(spot.coordinate.latitude),\(spot.coordinate.longitude)"
        urlString += "&q=\(spot.name.addingPercentEncoding(withAllowedCharacters: .alphanumerics)!)"
        let url = URL(string: urlString)!
        openURL(url)
    }
}
