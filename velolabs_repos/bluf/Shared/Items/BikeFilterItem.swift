//
//  BikeFilterItem.swift
//  BLUF
//
//  Created by Ravil Khusainov on 22.11.2020.
//

import SwiftUI

struct BikeFilterItem: View {
    let filter: Bike.Filter
    let delete: () -> Void
    var body: some View {
        HStack {
            Text(filter.key.title)
                .font(.headline)
            Text(filter.value)
                .font(.title3)
            Spacer()
            Button(action: delete, label: {
                Image(systemName: "minus.circle")
            })
            .font(.title2)
        }
    }
}

//struct FilterItem_Previews: PreviewProvider {
//    static var previews: some View {
//        FilterItem()
//    }
//}
