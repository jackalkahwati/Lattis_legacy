//
//  BikeItem.swift
//  BLUF
//
//  Created by Ravil Khusainov on 18.11.2020.
//

import SwiftUI

struct BikeItem: View {
    
    let bike: Bike
    
    var body: some View {
        VStack {
            HStack {
                Spacer()
                VStack {
                    Text(bike.name ?? "none")
                        .font(.headline)
                    Text(bike.fleet?.name ?? "none")
                        .font(.subheadline)
                }
                Spacer()
            }
            Spacer()
        }
        .foregroundColor(.white)
        .padding()
        .background(LinearGradient(gradient: Gradient(colors: [.blue, .lightBlue]), startPoint: .top, endPoint: .bottom))
        .cornerRadius(10)
    }
}

//struct BikeItem_Previews: PreviewProvider {
//    static var previews: some View {
//        BikeItem()
//    }
//}
