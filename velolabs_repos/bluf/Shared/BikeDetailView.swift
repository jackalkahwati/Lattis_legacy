//
//  BikeDetailView.swift
//  BLUF
//
//  Created by Ravil Khusainov on 18.11.2020.
//

import SwiftUI
import Combine

struct BikeDetailView: View {
    let bike: Bike
    @State var token: AnyCancellable?
    @State fileprivate var qrCodePresent = false
    
    var body: some View {
        VStack(alignment: .leading) {
            HStack(alignment: .top) {
                Text("\(bike.id)")
                Text(bike.name ?? "null")
            }
            .font(.title)
            if let fleet = bike.fleet {
                HStack {
                    Text(String(fleet.id))
                    Text(fleet.name ?? "No name")
                }
                .font(.headline)
            }
            if let qr = bike.qrCode {
                Button(action: showQRcode, label: {
                    Text("QR-code: " + String(qr))
                })
                .padding(.horizontal)
                .padding(.vertical, 5)
                .background(Color.blue.cornerRadius(3))
                .font(.subheadline)
                .foregroundColor(.white)
            }
            if let lock = bike.lockId {
                Button(action: showQRcode, label: {
                    Text("Lock: " + String(lock))
                })
                .padding(.horizontal)
                .padding(.vertical, 5)
                .background(Color.blue.cornerRadius(3))
                .font(.subheadline)
                .foregroundColor(.white)
            }
            if !bike.things.isEmpty {
                Button(action: showQRcode, label: {
                    Text("Things: " + String(bike.things.count))
                })
                .padding(.horizontal)
                .padding(.vertical, 5)
                .background(Color.blue.cornerRadius(3))
                .font(.subheadline)
                .foregroundColor(.white)
            }
            Spacer()
            Button(action: delete, label: {
                HStack {
                    Spacer()
                    Text("Delete")
                    Spacer()
                }
            })
            .padding()
            .background(
                Color.blue
                    .cornerRadius(5)
            )
            .font(.title3)
            .foregroundColor(.white)
        }
        .padding()
        .navigationTitle(bike.name ?? "None")
        .sheet(isPresented: $qrCodePresent, content: {
            QRCodeView(bike: bike)
        })
    }
    
    fileprivate func delete() {
//        token = CircleAPI.deleteBike(bike.id)
//            .sink(receiveCompletion: {_ in}, receiveValue: {_ in})
    }
    
    fileprivate func showQRcode() {
        qrCodePresent.toggle()
    }
}

struct BikeDetailView_Previews: PreviewProvider {
    static var previews: some View {
        BikeDetailView(bike: .init(id: 23, name: "Super bike kfkdsjf dkfjsdjfsdklflskdj ", qrCode: 12309, lockId: 345, things: [], fleet: nil))
    }
}
