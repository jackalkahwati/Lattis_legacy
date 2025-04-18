//
//  RentalSummaryView.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 2022-05-18.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import SwiftUI

struct RentalSummaryView: View {
    
    @StateObject var viewModel: ViewModel
    
    var body: some View {
        ZStack (alignment: .init(horizontal: .leading, vertical: .cardTop)) {
            VStack(alignment: .center) {
                VStack(alignment: .leading) {
                    if let end = viewModel.endedAt {
                        Text(end)
                            .font(.theme(weight: .bold, size: .small))
                    }
                    Text("ride_summary_trip_summary_label")
                        .font(.theme(weight: .bold, size: .giant))
                        .padding(.vertical, .margin/2)
                    if let price = viewModel.trip.price(for: .duration) {
                        row("bike_detail_label_price", value: Text(price))
                            .padding(.vertical, .margin/4)
                    }
                    if let price = viewModel.trip.price(for: .unlock) {
                        row("unlock_fee", value: Text(price))
                            .padding(.vertical, .margin/4)
                    }
                    
                    if let surcharge = viewModel.trip.price(for: .surcharge) {
                        row("surcharge", value: Text(surcharge))
                            .padding(.vertical, .margin/4)
                    }
                    
                    if let price = viewModel.trip.price(for: .parking) {
                        row("bike_detail_label_parking_fee", value: Text(price))
                            .padding(.vertical, .margin/4)
                    }
                    if let amount = viewModel.trip.discount, let price = viewModel.trip.discountString(amount) {
                        row("membership", value: Text(price))
                            .padding(.vertical, .margin/4)
                    }
                    if let amount = viewModel.trip.promoCodeDiscount, let price = viewModel.trip.discountString(amount) {
                        row("promo_code", value: Text(price))
                            .padding(.vertical, .margin/4)
                    }
                    if let taxes = viewModel.trip.taxes {
                        ForEach(taxes , id: \.taxId) { tax in
                            rowTax("\(tax.name)", value: "\(tax.amount)")
                                .padding(.vertical, .margin/4)
                        }
                    }
                    
                    if let price = viewModel.trip.price(for: .total) {
                        Capsule()
                            .foregroundColor(.gray.opacity(0.3))
                            .frame(height: 1)
                        HStack {
                            Text("ride_summary_total_label")
                                .font(.theme(weight: .medium, size: .text))
                            Spacer()
                            Text(price)
                                .font(.theme(weight: .bold, size: .text))
                        }
                        .foregroundColor(.black)
                        .padding(.vertical, .margin/4)
                        Capsule()
                            .foregroundColor(.gray.opacity(0.3))
                            .frame(height: 1)
                    }
                }
                .padding(.top)
                Text("rate_your_ride")
                    .font(.theme(weight: .book, size: .small))
                    .foregroundColor(.gray)
                    .padding(.vertical, .margin/4)
                HStack {
                    ForEach(1..<6) { idx in
                        Button {
                            viewModel.rating = idx
                        } label: {
                            Image(systemName: "star.fill")
                                .foregroundColor( viewModel.rating != nil && viewModel.rating! >= idx ? .black : .gray)
                        }
                    }
                }
                .font(.title)
                .padding(.bottom)
                Button {
                    viewModel.submit()
                } label: {
                    Text("submit")
                }
                .buttonStyle(.action())
            }
            .foregroundColor(.black)
            .padding()
            .background(
                Color.white
                    .cornerRadius(.containerCornerRadius, corners: [.topLeft, .topRight])
                    .shadow(color: .black.opacity(0.11), radius: 5, y: -5)
            )
            HStack {
                Text("ride_summary_duration_label")
                    .font(.theme(weight: .light, size: .text))
                Circle().fill(Color.white)
                    .frame(width: 4, height: 4)
                Text(viewModel.duration)
            }
            .font(.theme(weight: .bold, size: .text))
            .foregroundColor(.white)
            .padding()
            .frame(height: 38)
            .background(
                Color.black
                    .cornerRadius(10, corners: [.topRight, .bottomRight])
                    .shadow(radius: 2)
            )
            .padding(.top, -19)
            .alignmentGuide(.cardTop) { d in
                d[VerticalAlignment.top]
            }
        }
    }
    
    @ViewBuilder
    fileprivate func row<V: View>(_ title: LocalizedStringKey, value: V) -> some View {
        HStack {
            Text(title)
                .font(.theme(weight: .medium, size: .text))
            Spacer()
            value
                .font(.theme(weight: .bold, size: .text))
        }
        .foregroundColor(.black)
    }

    @ViewBuilder
    fileprivate func rowTax(_ title: String, value: String) -> some View {
        HStack {
            Text(title)
                .font(.theme(weight: .medium, size: .text))
            Spacer()
            Text(value)
                .font(.theme(weight: .bold, size: .text))
        }
        .foregroundColor(.black)
    }
}
